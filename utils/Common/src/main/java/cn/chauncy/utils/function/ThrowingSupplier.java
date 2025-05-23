package cn.chauncy.utils.function;

public interface ThrowingSupplier<T> {
    T get() throws Exception;
}