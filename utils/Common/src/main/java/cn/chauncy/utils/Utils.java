package cn.chauncy.utils;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class Utils {

    @SuppressWarnings("unchecked")
    public static <T> T castNullableTToT(Object obj) {
        return (T) obj;
    }

    public static <T, R> R getIfPresent(@Nullable T data, @NonNull Function<T, R> function) {
        return Optional.ofNullable(data).map(function).orElse(null);
    }

    public static <T, R, S> S getIfPresent(@Nullable T data, @NonNull Function<T, R> function, @NonNull Function<R, S> function2) {
        return Optional.ofNullable(data).map(function).map(function2).orElse(null);
    }

    public static <T, R, S, F> F getIfPresent(@Nullable T data, @NonNull Function<T, R> function, @NonNull Function<R, S> function2, @NonNull Function<S, F> function3) {
        return Optional.ofNullable(data).map(function).map(function2).map(function3).orElse(null);
    }

    public static void main(String[] args) {
        System.out.println(getIfPresent(null, Obj::getObj, Obj::getObj2, Obj::getObj3));
    }

    static class Obj {
        private Obj obj;
        private Obj obj2;
        private Obj obj3;
        public Object data;

        public Obj getObj() {
            return obj;
        }

        public Obj getObj2() {
            return obj2;
        }

        public Obj getObj3() {
            return obj3;
        }
    }
}
