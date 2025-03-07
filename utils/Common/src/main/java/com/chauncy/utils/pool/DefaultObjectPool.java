package com.chauncy.utils.pool;

import java.util.function.Supplier;

public class DefaultObjectPool<T extends Poolable> extends AbstractObjectPool<T>{

    public DefaultObjectPool(int maxSize, Supplier<T> supplier) {
        super(maxSize, supplier);
    }
}
