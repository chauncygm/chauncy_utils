package com.chauncy.utils.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class SimpleHandler implements ChannelInboundHandler, ChannelOutboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHandler.class);

    //region base handler
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
        logger.info("exceptionCaught: {}, cause: {}", ctx, ExceptionUtils.getRootCauseStackTraceList(cause));
    }
    //endregion

    //region inbound handler
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelRegistered: {}", ctx);
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelUnregistered: {}", ctx);
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive: {}", ctx);
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelInactive: {}", ctx);
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            msg = byteBuf.toString(0, byteBuf.readableBytes(), StandardCharsets.UTF_8);
        }
        logger.info("channelRead: {}, msg: {}", ctx, msg);
        ctx.fireChannelRead(msg);

        if (msg instanceof String tip && !tip.contains(": OK")) {
            ctx.writeAndFlush(msg + ": OK");
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        logger.info("channelReadComplete: {}", ctx);
        ctx.fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.info("userEventTriggered: {}, event: {}", ctx, evt);
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelWritabilityChanged: {}", ctx);
        ctx.fireChannelWritabilityChanged();
    }
    //endregion

    //region outbound handler
    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        logger.info("bind: {}, localAddress: {}, promise: {}", ctx, localAddress, promise);
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        logger.info("connect: {}, remoteAddress: {}, localAddress: {}, promise: {}", ctx, remoteAddress, localAddress, promise);
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        logger.info("disconnect: {}, promise: {}", ctx, promise);
        ctx.disconnect(promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        logger.info("close: {}, promise: {}", ctx, promise);
        ctx.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        logger.info("deregister: {}, promise: {}", ctx, promise);
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
