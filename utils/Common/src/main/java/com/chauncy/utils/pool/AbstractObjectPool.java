package com.chauncy.utils.pool;

import com.chauncy.utils.collection.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.concurrent.ThreadSafe;
import java.io.InvalidObjectException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@ThreadSafe
public abstract class AbstractObjectPool<T extends Poolable> implements ObjectPool<T>{

    private final Set<T> borrowedObjects = Collections.newSetFromMap(new WeakHashMap<>());
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final int poolInitSize;
    private final int poolMaxSize;
    private int createdInstanceCount;

    private final ArrayList<T> pool;
    private final Supplier<T> supplier;

    private ThreadLocal<T> currentObj = ThreadLocal.withInitial(() -> {
        T obj = get();
        borrowedObjects.add(obj);
        return obj;
    });

    public AbstractObjectPool(int maxSize, Supplier<T> supplier) {
        this(0, maxSize, supplier);
    }

    public AbstractObjectPool(int initSize, int maxSize, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        if (initSize < 0 || maxSize <= 0 || initSize > maxSize) {
            throw new IllegalArgumentException("initSize must be between 0 and maxSize");
        }

        this.poolInitSize = initSize;
        this.poolMaxSize = maxSize;
        this.pool = new ArrayList<>(initSize);
        this.supplier = supplier;

        initPool();
    }

    private void initPool() {
        for (int i = 0; i < poolInitSize; i++) {
            T e = newInstance();
            pool.add(e);
        }
}

    @Override
    public void returnObj(@NonNull T obj) {
        lock.writeLock().lock();
        try {
            if (CollectionUtils.identityContains(pool, obj)) {
                return;
            }

            obj.resetPoolable();
            if (pool.size() < poolMaxSize) {
                pool.add(obj);
            }
            borrowedObjects.remove(obj);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public T get() {
        lock.writeLock().lock();
        try {
            if (pool.isEmpty()) {
                T obj = newInstance();
                createdInstanceCount++;
                borrowedObjects.add(obj);
                return obj;
            }
            return pool.remove(pool.size() - 1);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private T newInstance() {
        try {
            T obj = supplier.get();
            if (obj == null) {
                throw new NullPointerException();
            }
            return obj;
        } catch (Exception e) {
            ExceptionUtils.asRuntimeException(new InvalidObjectException(""));
            return null;
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            pool.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int getMaxSize() {
        return poolMaxSize;
    }

    public int size() {
        lock.readLock().lock();
        try {
            return pool.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getCreatedInstanceCount() {
        lock.readLock().lock();
        try {
            return createdInstanceCount;
        } finally {
            lock.readLock().unlock();
        }
    }

}
