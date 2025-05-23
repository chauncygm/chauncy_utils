package com.chauncy.utils.net;

import com.chauncy.utils.net.codec.CrcChecker;
import com.chauncy.utils.net.codec.ProtobufCodec;
import com.chauncy.utils.net.handler.DispatcherHandler;
import com.chauncy.utils.net.handler.SimpleHandler;
import com.chauncy.utils.net.proto.MessageRegistry;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class SimplerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final MessageRegistry registry;
    private final boolean isServer;

    public SimplerChannelInitializer(MessageRegistry registry, boolean isServer) {
        this.registry = registry;
        this.isServer = isServer;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new IdleStateHandler(30, 30, 60));

        ch.pipeline().addLast(new LengthFieldPrepender(2));
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(4096, 0, 2, 0, 2));
        ch.pipeline().addLast(new CrcChecker());
        ch.pipeline().addLast(new ProtobufCodec(registry));
        ch.pipeline().addLast(new SimpleHandler(isServer));

        ch.pipeline().addLast(new DispatcherHandler());

    }
}
