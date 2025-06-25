package cn.chauncy.net;

import cn.chauncy.event.CtxMsgEvent;
import cn.chauncy.event.PlayerMsgEvent;
import cn.chauncy.logic.player.Player;
import cn.chauncy.message.ReqHeartbeat;
import cn.chauncy.message.ReqLogin;
import cn.chauncy.services.FixedExecutorService;
import cn.chauncy.utils.RateLimiter;
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

    private final FixedExecutorService loginExecutors = new FixedExecutorService("Login_Thread");
    private final FixedExecutorService logicExecutors = new FixedExecutorService("Logic_Thread");

    @Inject
    public GameMessageDispatcher(MessageRegistry registry) {
        super(registry);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtobufMessage<?> msg) {
        if (!(msg.message() instanceof ReqHeartbeat)) {
            logger.info("Ctx[{}] received message: {}", ctx, msg);
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
        if (message instanceof ReqLogin loginMsg) {
            loginExecutors.submit(loginMsg.getUid(), () -> registry.postMessageEvent(new CtxMsgEvent<>(ctx, message)));
        } else {
            Player player = ctx.channel().attr(Attrs.playerKey).get();
            if (player == null) {
                logger.error("player is null, message: {}", message);
                return;
            }
            logicExecutors.submit(player.getPlayerId(), () -> registry.postMessageEvent(new PlayerMsgEvent<>(player, message)));
        }
    }
}
