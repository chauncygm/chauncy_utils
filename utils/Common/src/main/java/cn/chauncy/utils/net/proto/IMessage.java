package cn.chauncy.utils.net.proto;

public interface IMessage {

    MessageType getType();

    byte[] serialize();

    enum MessageType {
        PROTOBUF
    }
}
