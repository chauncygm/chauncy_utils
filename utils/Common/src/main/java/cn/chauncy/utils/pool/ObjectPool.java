package cn.chauncy.utils.pool;

public interface ObjectPool<T extends Poolable> {

    T get();

    void returnObj(T obj);

    int size();

}
