package com.chauncy.utils.pool;

import com.chauncy.utils.collection.CollectionUtils;
import com.chauncy.utils.thread.ThreadUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@ThreadSafe
public class DefaultObjectPool<T extends Poolable> implements ObjectPool<T>{

    private static final Logger logger = LoggerFactory.getLogger(DefaultObjectPool.class);

    private static final Object NULL = (Poolable) () -> {};

    private final ConcurrentHashMap<T, Boolean> borrowedObjects = new ConcurrentHashMap<>();
    private final ThreadLocal<T> currentObj = ThreadLocal.withInitial(this::newInstance);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final int poolInitSize;
    private final int poolMaxSize;
    private final AtomicInteger pooledSize;
    private final AtomicInteger createdInstanceCount;

    private final ConcurrentLinkedQueue<T> pool;
    private final Supplier<T> supplier;

    public DefaultObjectPool(int maxSize, Supplier<T> supplier) {
        this(0, maxSize, supplier);
    }

    public DefaultObjectPool(int initSize, int maxSize, @NonNull Supplier<T> supplier) {
        if (initSize < 0 || maxSize <= 0 || initSize > maxSize) {
            throw new IllegalArgumentException("initSize must be between 0 and maxSize");
        }
        this.poolInitSize = initSize;
        this.poolMaxSize = maxSize;
        this.supplier = supplier;
        this.createdInstanceCount = new AtomicInteger();
        this.pooledSize = new AtomicInteger(initSize);
        this.pool = initPool(initSize);
    }

    private ConcurrentLinkedQueue<T> initPool(int initSize) {
        ConcurrentLinkedQueue<T> objects = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < initSize; i++) {
            T e = newInstance();
            objects.add(e);
        }
        return objects;
    }

    @Override
    public T get() {
        T t = currentObj.get();
        if (t != null) {
            //noinspection unchecked
            currentObj.set((T) NULL);
            return t;
        }

        lock.writeLock().lock();
        try {
            if (pool.isEmpty()) {
                T obj = newInstance();
                borrowedObjects.put(obj, true);
                return obj;
            }
            pooledSize.decrementAndGet();
            return pool.poll();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void returnObj(@NonNull T obj) {
        resetPoolable(obj);
        if (obj != NULL && currentObj.get() == NULL) {
            currentObj.set(obj);
        }

        lock.writeLock().lock();
        try {
            if (pool.contains(obj)) {
                return;
            }
            if (pooledSize.get() < poolMaxSize) {
                pool.offer(obj);
                pooledSize.incrementAndGet();
            } else {
                logger.warn("pool is full, can't add obj to pool. stacktrace:{}",
                        ThreadUtil.getCallerInfo(5));
            }
            borrowedObjects.remove(obj);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void resetPoolable(T obj) {
        try {
            obj.resetPoolable();
        } catch (Exception e) {
            logger.warn("reset obj error. stacktrace:{}", ExceptionUtils.getStackTrace(e));
        }
    }

    private T newInstance() {
        try {
            T obj = supplier.get();
            if (obj == null) {
                throw new NullPointerException();
            }
            createdInstanceCount.incrementAndGet();
            return obj;
        } catch (Exception e) {
            logger.error("supplier.get() occur error.", e);
            return ExceptionUtils.rethrow(e);
        }
    }

    public int size() {
        return pooledSize.get();
    }

    public int getPoolInitSize() {
        return poolInitSize;
    }

    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    public int getCreatedInstanceCount() {
        return createdInstanceCount.get();
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            pool.clear();
            borrowedObjects.clear();
            currentObj.remove();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DefaultObjectPool<Item> pool = new DefaultObjectPool<>(10, Item::new);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(() -> {
                for (int j = 1; j <= 10; j++) {
                    Item item = pool.get();
                    item.setId(j);
                    item.setNum(j);
                    System.out.println("use: " + item);
                    pool.returnObj(item);
                }
                latch.countDown();
            });
            t.start();
        }

        latch.await();
        System.out.println("pool size: " + pool.size());
        System.out.println("pool createdInstanceCount: " + pool.getCreatedInstanceCount());

    }

    static class Item implements Poolable {
        private int id;
        private int num;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "id=" + id +
                    ", num=" + num +
                    '}';
        }

        @Override
        public void resetPoolable() {
            this.id = 0;
            this.num = 0;
        }
    }

}
