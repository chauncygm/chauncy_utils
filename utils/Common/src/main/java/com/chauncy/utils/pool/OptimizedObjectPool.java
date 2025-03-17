
package com.chauncy.utils.pool;

import com.chauncy.utils.thread.ThreadUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

@ThreadSafe
public class OptimizedObjectPool<T extends Poolable> implements ObjectPool<T> {

    private static final Logger logger = LoggerFactory.getLogger(OptimizedObjectPool.class);
    
    private static final Poolable NULL_MARKER = () -> {};

    // 使用StampedLock替代ReadWriteLock（更好的并发性能）
    private final StampedLock stampedLock = new StampedLock();
    
    // 使用延迟初始化ThreadLocal
    private final ThreadLocal<T> currentObj = new ThreadLocal<>();
    
    // 使用弱引用+ReferenceQueue跟踪对象
    private final ReferenceQueue<T> refQueue = new ReferenceQueue<>();
    private final Map<WeakReference<T>, Boolean> borrowedObjects = Collections.synchronizedMap(new WeakHashMap<>());
    
    // 快速查找池中对象的索引
    private final Set<T> poolIndex = ConcurrentHashMap.newKeySet();
    
    private final int poolInitSize;
    private final int poolMaxSize;
    private final AtomicInteger pooledSize;
    private final LongAdder createdInstanceCount;
    private final ConcurrentLinkedQueue<T> pool;
    private final Supplier<T> factory;
    private final PoolFullPolicy<T> poolFullPolicy;

    public interface PoolFullPolicy<T> {
        void handleFullPool(T obj);
    }

    public OptimizedObjectPool(int maxPoolSize, @NonNull Supplier<T> factory) {
        this(0, maxPoolSize, factory);
    }

    public OptimizedObjectPool(int initSize, int maxSize, @NonNull Supplier<T> factory) {
        this(initSize, maxSize, factory, obj -> 
            logger.warn("Pool is full, discard object. Stacktrace:\n{}", ThreadUtil.getCallerInfo(5)));
    }

    public OptimizedObjectPool(int initSize, int maxSize, @NonNull Supplier<T> factory, 
                              PoolFullPolicy<T> poolFullPolicy) {
        if (initSize < 0 || maxSize <= 0 || initSize > maxSize) {
            throw new IllegalArgumentException("Invalid size params, initSize|maxSize: " + initSize + "|" + maxSize);
        }
        this.poolInitSize = initSize;
        this.poolMaxSize = maxSize;
        this.factory = factory;
        this.poolFullPolicy = poolFullPolicy;
        this.pooledSize = new AtomicInteger(initSize);
        this.createdInstanceCount = new LongAdder();
        this.pool = initPool(initSize);
    }

    private ConcurrentLinkedQueue<T> initPool(int initSize) {
        ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < initSize; i++) {
            T obj = safeCreateInstance();
            queue.add(obj);
            poolIndex.add(obj);
        }
        return queue;
    }

    @Override
    public T get() {
        // 尝试ThreadLocal优先
        T threadLocalObj = currentObj.get();
        if (threadLocalObj != null && threadLocalObj != NULL_MARKER) {
            //noinspection unchecked
            currentObj.set((T) NULL_MARKER);
            return threadLocalObj;
        }

        long stamp = stampedLock.writeLock();
        try {
            T obj = pool.poll();
            if (obj != null) {
                pooledSize.decrementAndGet();
                poolIndex.remove(obj);
                trackBorrowedObject(obj);
                return obj;
            }
            obj = safeCreateInstance();
            trackBorrowedObject(obj);
            return obj;
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

    @Override
    public void returnObj(@NonNull T obj) {
        resetPoolable(obj);
        if (obj != NULL_MARKER && currentObj.get() == NULL_MARKER) {
            currentObj.set(obj);
            return;
        }

        final long stamp = stampedLock.writeLock();
        try {
            if (!isBorrowedObject(obj)) {
                logger.warn("Return invalid object");
                return;
            }

            if (pooledSize.get() < poolMaxSize && pool.offer(obj)) {
                pooledSize.incrementAndGet();
                poolIndex.add(obj);
            } else {
                poolFullPolicy.handleFullPool(obj);
            }
            borrowedObjects.put(new WeakReference<>(obj, refQueue), false);
        } finally {
            stampedLock.unlockWrite(stamp);
            cleanExpiredReferences();
        }
    }

    // 清理已回收的弱引用
    private void cleanExpiredReferences() {
        WeakReference<?> ref;
        while ((ref = (WeakReference<?>) refQueue.poll()) != null) {
            borrowedObjects.remove(ref);
        }
    }

    // 跟踪借出对象
    private void trackBorrowedObject(T obj) {
        borrowedObjects.put(new WeakReference<>(obj, refQueue), true);
    }

    private boolean isBorrowedObject(T obj) {
        return borrowedObjects.keySet().stream()
            .map(WeakReference::get)
            .anyMatch(o -> o == obj);
    }

    private T safeCreateInstance() {
        try {
            T obj = Objects.requireNonNull(factory.get());
            createdInstanceCount.increment();
            return obj;
        } catch (Exception e) {
            logger.error("Object creation failed", e);
            throw new RuntimeException("Object creation failed", e);
        }
    }

    private void resetPoolable(T obj) {
        try {
            obj.resetPoolable();
        } catch (Exception e) {
            logger.error("reset obj error. stacktrace:\n{}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Object reset failed", e);
        }
    }

    // 其他监控方法保持类似，但使用poolIndex优化查询
    public int getOutsidePoolSize() {
        cleanExpiredReferences();
        return (int) borrowedObjects.entrySet().stream()
            .filter(entry -> {
                T obj = entry.getKey().get();
                return obj != null && !entry.getValue() && !poolIndex.contains(obj);
            }).count();
    }

    @Override
    public int size() {
        return pooledSize.get();
    }

    public int getCreatedInstanceCount() {
        return createdInstanceCount.intValue();
    }

    public int getPoolInitSize() {
        return poolInitSize;
    }

    public int getPoolMaxSize() {
        return poolMaxSize;
    }
}