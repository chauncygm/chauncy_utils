package cn.chauncy.utils.function;

public interface ThrowingConsumer<T> {
    void consume(T obj) throws Exception;
}