package com.chauncy.utils.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

public class SimplerChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) {
            ch.pipeline().addLast(new IdleStateHandler(30, 30, 30));
            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(4096, 0, 2, 0, 2));
            ch.pipeline().addLast(new SimpleEncoder());
            ch.pipeline().addLast(new SimpleHandler());
        }
}
