package com.chauncy.utils.function;

import com.chauncy.utils.common.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

public class FunctionUtils {

    private FunctionUtils() {}

    public static <T> T wrap(@NonNull ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return ExceptionUtils.rethrow(e);
        }
    }

    public static <T> void wrap(@NonNull ThrowingConsumer<T> consumer, T obj) {
        try {
            consumer.consume(obj);
        } catch (Exception e) {
            ExceptionUtils.rethrow(e);
        }
    }
}



