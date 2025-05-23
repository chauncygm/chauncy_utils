package cn.chauncy.utils.net.handler;

import cn.chauncy.utils.net.proto.ProtobufMessage;
import io.netty.channel.ChannelHandlerContext;

public interface MessageHandler {

    void handler(ChannelHandlerContext ctx, ProtobufMessage<?> message);

}
