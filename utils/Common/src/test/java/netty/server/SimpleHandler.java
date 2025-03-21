package netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHandler implements ChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHandler.class);

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelRegistered: {}", ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelUnregistered: {}", ctx);

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive: {}", ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelInactive: {}", ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("channelRead: {}", ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelReadComplete: {}", ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.info("userEventTriggered: {}", ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelWritabilityChanged: {}", ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("handlerAdded: {}", ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        logger.info("handlerRemoved: {}", ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("exceptionCaught: {}", ctx);
    }
}
