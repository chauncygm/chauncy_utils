package com.chauncy.utils.function;

import com.chauncy.utils.common.ExceptionUtils;

public class FunctionUtils {

    private FunctionUtils() {}

    public static <T> T wrap(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return ExceptionUtils.rethrow(e);
        }
    }

    public static <T> void wrap(ThrowingConsumer<T> consumer, T obj) {
        try {
            consumer.consume(obj);
        } catch (Exception e) {
            ExceptionUtils.rethrow(e);
        }
    }
}



