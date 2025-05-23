package cn.chauncy.utils.common;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

    @SuppressWarnings("unchecked")
    public static <R, T extends Throwable> R eraseType(@NonNull final Throwable throwable) throws T {
        throw (T)throwable;
    }

    public static <T> T rethrow(@NonNull final Throwable throwable) {
        return ExceptionUtils.eraseType(throwable);
    }

    public static String getStackTrace(final Throwable throwable) {
        if (throwable == null) {
            return StringUtils.EMPTY;
        }
        final StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
}
