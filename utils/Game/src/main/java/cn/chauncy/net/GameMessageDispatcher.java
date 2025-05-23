package cn.chauncy.net;

import cn.chauncy.event.PlayerMsgEvent;
import cn.chauncy.utils.net.handler.MessageDispatcher;
import cn.chauncy.utils.net.proto.MessageRegistry;
import cn.chauncy.utils.net.proto.ProtobufMessage;
import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class GameMessageDispatcher extends MessageDispatcher<ProtobufMessage<?>> {

    private static final Logger logger = LoggerFactory.getLogger(GameMessageDispatcher.class);

    private final AtomicInteger totalCount = new AtomicInteger(0);

    @Inject
    public GameMessageDispatcher(MessageRegistry registry) {
        super(registry);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtobufMessage<?> msg) {
        logger.info("ctx: {}, message: {}", ctx, msg);
        registry.postMessageEvent(new PlayerMsgEvent<>(null, msg.message()));
    }
}
