package com.chauncy.utils.net.handler;

import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {

    }
}
