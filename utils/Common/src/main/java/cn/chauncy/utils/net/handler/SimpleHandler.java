package cn.chauncy.utils.net.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class SimpleHandler implements ChannelInboundHandler, ChannelOutboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHandler.class);

    //region base handler
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.info("handlerAdded: {}", ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger.debug("handlerRemoved: {}", ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.debug("exceptionCaught: {}, cause: {}", ctx, cause);
    }
    //endregion

    //region inbound handler
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        logger.debug("channelRegistered: {}", ctx);
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        logger.debug("channelUnregistered: {}", ctx);
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug("channelActive: {}", ctx);
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.debug("channelInactive: {}", ctx);
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.debug("channelRead: {}, msg: {}", ctx, msg);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
//        logger.info("channelReadComplete: {}", ctx);
        ctx.fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            logger.debug("userEventTriggered: {}, event: {}", ctx, evt);
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        logger.debug("channelWritabilityChanged: {}", ctx);
        ctx.fireChannelWritabilityChanged();
    }
    //endregion

    //region outbound handler
    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
        logger.debug("bind: {}, localAddress: {}, promise: {}", ctx, localAddress, promise);
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        logger.debug("connect: {}, remoteAddress: {}, localAddress: {}, promise: {}", ctx, remoteAddress, localAddress, promise);
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
        logger.debug("disconnect: {}, promise: {}", ctx, promise);
        ctx.disconnect(promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
        logger.debug("close: {}, promise: {}", ctx, promise);
        ctx.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        logger.debug("deregister: {}, promise: {}", ctx, promise);
        ctx.deregister(promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
//        logger.info("read: {}", ctx);
        ctx.read();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        logger.info("write: {}, msg: {}, promise: {}", ctx, msg, promise);
        ctx.write(msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
//        logger.info("flush: {}", ctx);
        ctx.flush();
    }
    //endregion
}
