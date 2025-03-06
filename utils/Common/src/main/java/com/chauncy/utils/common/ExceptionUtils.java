package com.chauncy.utils.common;

public class ExceptionUtils {

    @SuppressWarnings("unchecked")
    public static <R, T extends Throwable> R eraseType(final Throwable throwable) throws T {
        throw (T)throwable;
    }

    public static <T> T rethrow(final Throwable throwable) {
        return ExceptionUtils.eraseType(throwable);
    }
}
