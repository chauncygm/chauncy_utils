package cn.chauncy.utils.net.proto;

import com.google.protobuf.Message;

public record ProtobufMessage<T extends Message>(int protoEnum, T message) implements IMessage {
    @Override
    public MessageType getType() {
        return MessageType.PROTOBUF;
    }

    @Override
    public byte[] serialize() {
        return message.toByteArray();
    }
}
