package com.chauncy.utils.obj_pool;

public interface ObjectPool<T extends Poolable> {

    T get();

    void returnObj(T obj);

    int size();

}
