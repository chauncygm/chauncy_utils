package cn.chauncy.utils.net.codec;

import cn.chauncy.utils.net.proto.ProtobufMessage;
import cn.chauncy.utils.net.proto.MessageRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProtobufCodec extends ByteToMessageCodec<Message> {

    private static final Logger logger = LoggerFactory.getLogger(ProtobufCodec.class);

    private final MessageRegistry registry;

    public ProtobufCodec(MessageRegistry registry) {
        this.registry  = registry;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        int protoEnum = registry.getProtoEnum(msg.getClass());
        byte[] byteArray = msg.toByteArray();
        out.writeInt(protoEnum);
        out.writeBytes(byteArray);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }

        int protoEnum = in.readInt();
        int length = in.readableBytes();
        Parser<? extends Message> parser = registry.getParser(protoEnum);
        if (parser == null) {
            logger.error("unknown msg, ctx: {}, protoEnum: {}", ctx, protoEnum);
            in.skipBytes(length);
            return;
        }

        byte[] array = new byte[length];
        in.readBytes(array);
        try {
            Message message = parser.parseFrom(array);
            out.add(new ProtobufMessage<>(protoEnum, message));
        } catch (Exception e) {
            logger.error("parse message error, ctx: {}, protoEnum: {}", ctx, protoEnum, e);
            in.skipBytes(length);
        }
    }
}
