package com.chauncy.utils.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;
import java.util.zip.CRC32;

public class CrcChecker extends ByteToMessageCodec<ByteBuf> {

    private static final int CRC_LENGTH = 8; // CRC32 as long

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int readableBytes = msg.readableBytes();
        byte[] array = new byte[readableBytes];
        msg.markReaderIndex();
        msg.readBytes(array);
        msg.resetReaderIndex();

        long crc = calculateCrc32(array);

        out.writeBytes(msg.nioBuffer());
        out.writeLong(crc);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < CRC_LENGTH) {
            return; // 需要至少一个 CRC 值
        }

        int endIndex = in.writerIndex();
        in.markReaderIndex();
        int length = endIndex - CRC_LENGTH;

        if (length <= 0) {
            in.resetReaderIndex();
            return;
        }

        // 读取 payload 数据
        byte[] payload = new byte[length];
        in.readBytes(payload);

        // 读取 CRC
        long receivedCrc = in.readLong();

        // 计算实际 CRC
        long calculatedCrc = calculateCrc32(payload);

        if (receivedCrc != calculatedCrc) {
            throw new RuntimeException("CRC mismatch: expected " + calculatedCrc + ", got " + receivedCrc);
        }

        // 将校验通过的数据重新包装成 ByteBuf 返回给下一个 handler
        ByteBuf buffer = ctx.alloc().buffer(length);
        buffer.writeBytes(payload);
        out.add(buffer);
    }

    public static long calculateCrc32(byte[] data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return crc32.getValue();
    }
}
