package cn.chauncy.utils.net.proto;

import com.google.protobuf.Message;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@FunctionalInterface
public interface MessageParser<T extends Message> {

    T parseFrom(byte[] data);


    static MessageParser<?> create(Class<? extends Message> clazz) {
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
            return null;
        }
    }
}
