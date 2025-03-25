package com.chauncy.utils.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public class SimpleEncoder extends MessageToByteEncoder<String> {

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        ByteBuf bytebuf = ctx.alloc().buffer();
        bytebuf.writeShort(msg.length());
        bytebuf.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
        out.writeBytes(bytebuf);
    }
}
