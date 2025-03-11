package com.chauncy.utils.common;

import org.checkerframework.checker.nullness.qual.NonNull;

public class ExceptionUtils {

    @SuppressWarnings("unchecked")
    public static <R, T extends Throwable> R eraseType(@NonNull final Throwable throwable) throws T {
        throw (T)throwable;
    }

    public static <T> T rethrow(@NonNull final Throwable throwable) {
        return ExceptionUtils.eraseType(throwable);
    }
}
