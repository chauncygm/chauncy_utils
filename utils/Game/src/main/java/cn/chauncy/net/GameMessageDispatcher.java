package cn.chauncy.net;

import cn.chauncy.event.CtxMsgEvent;
import cn.chauncy.logic.player.Player;
import cn.chauncy.message.ReqHeartbeat;
import cn.chauncy.message.ReqLogin;
import cn.chauncy.services.GameTickEventDispatcherService;
import cn.chauncy.utils.RateLimiter;
import cn.chauncy.utils.eventbus.GenericEvent;
import cn.chauncy.utils.net.handler.MessageDispatcher;
import cn.chauncy.utils.net.proto.MessageRegistry;
import cn.chauncy.utils.net.proto.ProtobufMessage;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@ChannelHandler.Sharable
public class GameMessageDispatcher extends MessageDispatcher<ProtobufMessage<?>> {

    private static final Logger logger = LoggerFactory.getLogger(GameMessageDispatcher.class);

    private static final int MAX_COUNT_PER_SECOND = 4;
    private final Map<Integer, LongAdder> msgCountMap = new ConcurrentHashMap<>();
    private final GameTickEventDispatcherService dispatcherService;


    @Inject
    public GameMessageDispatcher(MessageRegistry registry, GameTickEventDispatcherService dispatcherService) {
        super(registry);
        this.dispatcherService = dispatcherService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtobufMessage<?> msg) {
        if (!(msg.message() instanceof ReqHeartbeat)) {
            logger.info("Ctx[{}] received message: {}", ctx.channel().id(), msg.message());
        }
        Attribute<Map<Integer, RateLimiter>> attr = ctx.channel().attr(Attrs.rateLimiterMapKey);
        Map<Integer, RateLimiter> rateLimiterMap = attr.get();
        if (rateLimiterMap == null) {
            rateLimiterMap =new HashMap<>();
            rateLimiterMap.put(msg.protoEnum(), new RateLimiter(MAX_COUNT_PER_SECOND));
            attr.set(rateLimiterMap);
        }
        RateLimiter rateLimiter = rateLimiterMap.get(msg.protoEnum());
        if (rateLimiter == null) {
            rateLimiter = new RateLimiter(MAX_COUNT_PER_SECOND);
        }
        if (!rateLimiter.allowOperation()) {
            logger.warn("rateLimiter not allow operation, msg: {}", msg);
            return;
        }
        msgCountMap.computeIfAbsent(msg.protoEnum(), (k) -> new LongAdder()).increment();


        Message message = msg.message();
        Player player = ctx.channel().attr(Attrs.playerKey).get();
        CtxMsgEvent<Message> data = new CtxMsgEvent<>(ctx, player, message);
        dispatcherService.postEvent(GameTickEventDispatcherService.GlobalEvent.EventType.PLAYER_MSG_EVENT, msg.protoEnum(), data);
    }
}
