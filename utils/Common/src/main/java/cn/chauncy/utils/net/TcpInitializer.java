package cn.chauncy.utils.net;

import cn.chauncy.utils.net.codec.CrcChecker;
import cn.chauncy.utils.net.codec.ProtobufCodec;
import cn.chauncy.utils.net.handler.MessageDispatcher;
import cn.chauncy.utils.net.handler.SimpleHandler;
import cn.chauncy.utils.net.proto.MessageRegistry;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class TcpInitializer extends ChannelInitializer<SocketChannel> {

    private final MessageRegistry registry;
    private final MessageDispatcher<?> dispatcher;

    public TcpInitializer(MessageDispatcher<?> dispatcher) {
        this.registry = dispatcher.getRegistry();
        this.dispatcher = dispatcher;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new IdleStateHandler(30, 30, 60));

        ch.pipeline().addLast(new LengthFieldPrepender(2));
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(4096, 0, 2, 0, 2));
        ch.pipeline().addLast(new CrcChecker());
        ch.pipeline().addLast(new ProtobufCodec(registry));
        ch.pipeline().addLast(new SimpleHandler());

        ch.pipeline().addLast(dispatcher);

    }
}
