package cn.chauncy.utils.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class SimpleStringCodec extends ByteToMessageCodec<String> {

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) {
        out.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int length = in.readableBytes();
        String msg = in.readBytes(length).toString(StandardCharsets.UTF_8);
        out.add(msg);
    }
}
