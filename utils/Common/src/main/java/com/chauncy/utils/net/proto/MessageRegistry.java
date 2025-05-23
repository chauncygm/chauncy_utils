package com.chauncy.utils.net.proto;

import com.google.protobuf.Message;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MessageRegistry {

    private final Int2ObjectOpenHashMap<Class<? extends Message>> messageMap = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<MessageParser<? extends Message>> parserMap = new Int2ObjectOpenHashMap<>();

    public void register(Class<? extends Message> clazz) {
        int protoEnum = getProtoEnum(clazz);
        if (messageMap.containsKey(protoEnum)) {
            Class<? extends Message> lastClazz = messageMap.get(protoEnum);
            throw new IllegalArgumentException("protoEnum repeated, last:" + lastClazz +  ", current: " + clazz);
        }

        MessageParser<?> parser = createParser(clazz);
        parserMap.put(protoEnum, parser);
        messageMap.put(protoEnum, clazz);
    }

    public int getProtoEnum(Class<? extends Message> clazz) {
        return clazz.getSimpleName().hashCode();
    }

    public MessageParser<? extends Message> getParser(int protoEnum) {
        return parserMap.get(protoEnum);
    }

    public Class<? extends Message> getMessageClass(int protoEnum) {
        return messageMap.get(protoEnum);
    }

    private MessageParser<?> createParser(Class<? extends Message> clazz) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            MethodHandle mh = lookup.findStatic(clazz, "parseFrom", MethodType.methodType(clazz, byte[].class));
            // 使用 LambdaMetafactory 创建函数式接口实例
            MethodType funcType = MethodType.methodType(Message.class, byte[].class);
            MethodType invokedType = MethodType.methodType(MessageParser.class);
            MethodHandle factory = LambdaMetafactory.metafactory(
                    lookup,
                    "parseFrom",
                    invokedType,
                    funcType,
                    mh,
                    funcType
            ).getTarget();

            return (MessageParser<? extends Message>) factory.invoke();

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


}
