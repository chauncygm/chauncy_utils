package cn.chauncy.utils.pool;

import cn.chauncy.utils.thread.ThreadUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@ThreadSafe
public class DefaultObjectPool<T extends Poolable> implements ObjectPool<T>{

    private static final Logger logger = LoggerFactory.getLogger(DefaultObjectPool.class);

    private static final Object NULL = (Poolable) () -> {};

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /** 线程私有的实例 */
    private final ThreadLocal<T> currentObj = ThreadLocal.withInitial(this::safeCreateInstance);
    /** 所有新创建的实例状态 */
    private final ConcurrentHashMap<T, Boolean> borrowedObjects = new ConcurrentHashMap<>();

    /** 对象池初始大小 */
    private final int poolInitSize;
    /** 对象池最大容量 */
    private final int poolMaxSize;
    /** 对象池实例数 */
    private final AtomicInteger pooledSize;
    /** 创建的实例总数 */
    private final LongAdder createdInstanceCount;

    /** 对象池 */
    private final ConcurrentLinkedQueue<T> pool;
    /** 对象工厂 */
    private final Supplier<T> factory;

    /**
     * 对象池，默认初始池大小为0
     *
     * @param maxSize 对象池最大容量
     * @param factory 创建实例工厂
     */
    public DefaultObjectPool(int maxSize, Supplier<T> factory) {
        this(0, maxSize, factory);
    }

    /**
     * 对象池
     *
     * @param initSize 初始化大小
     * @param maxSize 对象池最大容量
     * @param factory 创建实例工厂
     */
    public DefaultObjectPool(int initSize, int maxSize, @NonNull Supplier<T> factory) {
        if (initSize < 0 || maxSize <= 0 || initSize > maxSize) {
            throw new IllegalArgumentException("initSize(" + initSize + ") must be between 0 and maxSize(" + maxSize + ")");
        }
        this.poolInitSize = initSize;
        this.poolMaxSize = maxSize;
        this.factory = factory;
        this.pooledSize = new AtomicInteger(initSize);
        this.createdInstanceCount = new LongAdder();
        this.pool = initPool(initSize);
    }

    private ConcurrentLinkedQueue<T> initPool(int initSize) {
        ConcurrentLinkedQueue<T> objects = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < initSize; i++) {
            T e = safeCreateInstance();
            objects.add(e);
        }
        return objects;
    }

    /**
     * 获取实例
     */
    @Override
    public T get() {
        T t = currentObj.get();
        if (t != NULL) {
            //noinspection unchecked
            currentObj.set((T) NULL);
            return t;
        }

        lock.writeLock().lock();
        try {
            T obj = pool.poll();
            if (obj != null) {
                pooledSize.decrementAndGet();
            } else {
                obj = safeCreateInstance();
            }
            borrowedObjects.put(obj, true);
            return obj;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 归还实例
     * 必须是通过此对象池借出的对象才能归还
     * 如果对象池已满，则直接丢弃，设置跟踪对象的借出状态为false
     */
    @Override
    public void returnObj(@NonNull T obj) {
        resetPoolable(obj);
        if (obj != NULL && currentObj.get() == NULL) {
            currentObj.set(obj);
            return;
        }

        lock.writeLock().lock();
        try {
            if (!borrowedObjects.containsKey(obj)) {
                logger.warn("obj is not borrowed, can't return obj to pool. stacktrace:\n{}", ThreadUtil.getCallerInfo(5));
                return;
            }
            if (pool.contains(obj)) {
                return;
            }
            borrowedObjects.put(obj, false);
            if (pooledSize.get() < poolMaxSize) {
                pool.offer(obj);
                pooledSize.incrementAndGet();
            } else {
                logger.warn("pool is full, can't add obj to pool. stacktrace:\n{}", ThreadUtil.getCallerInfo(5));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void resetPoolable(T obj) {
        try {
            obj.resetPoolable();
        } catch (Exception e) {
            logger.warn("reset obj error. stacktrace:\n{}", ExceptionUtils.getStackTrace(e));
            ExceptionUtils.wrapAndThrow(e);
        }
    }

    private T safeCreateInstance() {
        try {
            T obj = factory.get();
            if (obj == null) {
                throw new NullPointerException();
            }
            createdInstanceCount.increment();
            return obj;
        } catch (Exception e) {
            logger.error("factory.get() occur error.", e);
            return ExceptionUtils.rethrow(e);
        }
    }

    /** 获取对象池的初始大小 */
    public int getPoolInitSize() {
        return poolInitSize;
    }

    /** 获取对象池的最大容量 */
    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    /** 获取对象池中可用的实例数 */
    public int size() {
        return pooledSize.get();
    }

    /** 获取此对象池已创建实例的数量，不包含ThreadLocal中的实例 */
    public long getCreatedInstanceCount() {
        return createdInstanceCount.longValue();
    }

    /** 获取已归还不再使用且不在对象池中管理的对象数 */
    public int getOutsidePoolSize() {
        lock.readLock().lock();
        try {
            int size = 0;
            for (Map.Entry<T, Boolean> entry : borrowedObjects.entrySet()) {
                if (!entry.getValue() && !pool.contains(entry.getKey())) {
                    size++;
                }
            }
            return size;
        } finally {
            lock.readLock().unlock();
        }
    }

    /** 获取归还不再使用且不在对象池中管理的所有对象 */
    public Collection<T> getAllOutsidePool() {
        Collection<T> objs = new ArrayList<>(borrowedObjects.size());
        lock.readLock().lock();
        try {
            for (Map.Entry<T, Boolean> entry : borrowedObjects.entrySet()) {
                if (!entry.getValue() && !pool.contains(entry.getKey())) {
                    objs.add(entry.getKey());
                }
            }
            return objs;
        } finally {
            lock.readLock().unlock();
        }
    }

    /** 清理已归还不再使用且不在对象池中管理的所有对象 */
    public void clearOutsidePool() {
        lock.writeLock().lock();
        try {
            borrowedObjects.entrySet().removeIf(entry -> !entry.getValue() && !pool.contains(entry.getKey()));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** 清理对象池 */
    public void clear() {
        lock.writeLock().lock();
        try {
            pool.clear();
            pooledSize.set(0);
            createdInstanceCount.reset();
            borrowedObjects.clear();
            currentObj.remove();
        } finally {
            lock.writeLock().unlock();
        }
    }

}
