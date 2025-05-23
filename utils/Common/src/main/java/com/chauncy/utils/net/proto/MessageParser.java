package com.chauncy.utils.net.proto;

import com.google.protobuf.Message;

@FunctionalInterface
public interface MessageParser<T extends Message> {
    T parseFrom(byte[] data);
}
