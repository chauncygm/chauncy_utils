package cn.chauncy.utils.net.handler;

import cn.chauncy.utils.net.proto.IMessage;
import cn.chauncy.utils.net.proto.MessageRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageDispatcher<T extends IMessage> extends SimpleChannelInboundHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    protected MessageRegistry registry;

    public MessageDispatcher(MessageRegistry registry) {
        this.registry = registry;
    }

    public MessageRegistry getRegistry() {
        return registry;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("channelActive: {}", ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("MessageDispatcher exceptionCaught, ctx: {}, cause: {}", ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("channelInactive: {}", ctx);
    }
}
