package cn.chauncy.net;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.event.CtxMsgEvent;
import cn.chauncy.event.PlayerEvent;
import cn.chauncy.logic.player.Player;
import cn.chauncy.message.ReqHeartbeat;
import cn.chauncy.message.ResHeartbeat;
import cn.chauncy.services.GameTickEventDispatcherService;
import cn.chauncy.utils.RateLimiter;
import cn.chauncy.utils.net.handler.MessageDispatcher;
import cn.chauncy.utils.net.proto.MessageRegistry;
import cn.chauncy.utils.net.proto.ProtobufMessage;
import cn.chauncy.utils.time.TimeProvider;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@ChannelHandler.Sharable
public class GameMessageDispatcher extends MessageDispatcher<ProtobufMessage<?>> {

    private static final Logger logger = LoggerFactory.getLogger(GameMessageDispatcher.class);

    private static final int MAX_COUNT_MSG_PER_SECOND = 4;
    private static final int MAX_COUNT_HEARTBEAT_PER_MINUTE = 12;
    private final Map<Integer, LongAdder> msgCountMap = new ConcurrentHashMap<>();
    private final GameTickEventDispatcherService dispatcherService;
    private final TimeProvider timeProvider;

    private final int heartBeatProtoEnum;

    private final GlobalEventBus globalEventBus;


    @Inject
    public GameMessageDispatcher(MessageRegistry registry, GameTickEventDispatcherService dispatcherService, TimeProvider timeProvider, GlobalEventBus globalEventBus) {
        super(registry);
        this.dispatcherService = dispatcherService;
        this.timeProvider = timeProvider;
        this.heartBeatProtoEnum = registry.getProtoEnum(ReqHeartbeat.class);
        this.globalEventBus = globalEventBus;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtobufMessage<?> msg) {
        boolean isHeartbeat = isHeartbeat(msg);
        Map<Integer, RateLimiter> rateLimiterMap = ctx.channel().attr(Attrs.rateLimiterMapKey).get();
        if (rateLimiterMap == null) {
            rateLimiterMap = new HashMap<>();
            ctx.channel().attr(Attrs.rateLimiterMapKey).set(rateLimiterMap);
        }
        RateLimiter rateLimiter = rateLimiterMap.get(msg.protoEnum());
        if (rateLimiter == null) {
            rateLimiter = new RateLimiter(MAX_COUNT_MSG_PER_SECOND, 1000);
            if (msg.protoEnum() == heartBeatProtoEnum) {
                rateLimiter = new RateLimiter(MAX_COUNT_HEARTBEAT_PER_MINUTE, 60 * 1000);
            }
            rateLimiterMap.put(msg.protoEnum(), rateLimiter);
        }
        if (!rateLimiter.allowOperation()) {
            logger.warn("rateLimiter not allow operation, msg: {}", msg);
            return;
        }
        msgCountMap.computeIfAbsent(msg.protoEnum(), (k) -> new LongAdder()).increment();

        if (isHeartbeat) {
            heartbeat(ctx);
            return;
        }

        Message message = msg.message();
        Player player = ctx.channel().attr(Attrs.playerKey).get();
        CtxMsgEvent<Message> data = new CtxMsgEvent<>(ctx, player, message);
        dispatcherService.postEvent(GameTickEventDispatcherService.GlobalEvent.EventType.PLAYER_MSG_EVENT, msg.protoEnum(), data);
    }

    @Override
    protected boolean isHeartbeat(ProtobufMessage<?> msg) {
        return msg.protoEnum() == heartBeatProtoEnum;
    }

    @Override
    protected void heartbeat(ChannelHandlerContext ctx) {
        Player player = ctx.channel().attr(Attrs.playerKey).get();
        if (player == null) {
            logger.error("receive heartbeat but player is null");
            return;
        }

        ResHeartbeat.Builder builder = ResHeartbeat.newBuilder();
        builder.setTime(timeProvider.getTimeMillis());
        ctx.writeAndFlush(builder.build());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Player player = ctx.channel().attr(Attrs.playerKey).get();
        if (player != null && player.isOnline()) {
            logger.info("Player offline due to channel inactive: {}", player.info());
            globalEventBus.post(new PlayerEvent.PlayerOfflineEvent(player));
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent idleStateEvent) {
            logger.warn("IdleStateEvent triggered: {}, channel will be closed", idleStateEvent);
            Player player = ctx.channel().attr(Attrs.playerKey).get();
            if (player != null && player.isOnline()) {
                globalEventBus.post(new PlayerEvent.PlayerOfflineEvent(player));
            }
        }
        ctx.fireUserEventTriggered(evt);
    }
}
