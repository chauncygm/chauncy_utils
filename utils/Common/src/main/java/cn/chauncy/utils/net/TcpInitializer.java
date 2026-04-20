package cn.chauncy.utils.net;

import cn.chauncy.utils.net.codec.CrcChecker;
import cn.chauncy.utils.net.codec.ProtobufCodec;
import cn.chauncy.utils.net.handler.MessageDispatcher;
import cn.chauncy.utils.net.handler.SimpleHandler;
import cn.chauncy.utils.net.proto.MessageRegistry;
import cn.chauncy.utils.net.proto.ProtobufMessage;
import com.google.inject.Inject;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class TcpInitializer extends ChannelInitializer<SocketChannel> {

    private final MessageRegistry registry;
    private final MessageDispatcher<ProtobufMessage<?>> dispatcher;

    @Inject
    public TcpInitializer(MessageDispatcher<ProtobufMessage<?>> dispatcher) {
        this.registry = dispatcher.getRegistry();
        this.dispatcher = dispatcher;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
//        ch.pipeline().addLast(new IdleStateHandler(30, 30, 60));

        // 数据格式 |2bit 包长度 (| (4bit 协议号 | x bit proto数据) 计算 | 8bit crc校验码 |)
        ch.pipeline().addLast(new LengthFieldPrepender(2));
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(4096, 0, 2, 0, 2));
        ch.pipeline().addLast(new CrcChecker());
        ch.pipeline().addLast(new ProtobufCodec(registry));
        ch.pipeline().addLast(new SimpleHandler());

        ch.pipeline().addLast(dispatcher);

    }
}
