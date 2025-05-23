package cn.chauncy.utils.redis;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Callable;

public class RedisUtils {

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    private static RedissonClient redissonClient;

    private RedisUtils() {}

    public static void init(RedissonClient redissonClient) {
        RedisUtils.redissonClient = redissonClient;
    }

    public static <V> V tryLock(String lockKey, Callable<V> callable) {
        Objects.requireNonNull(redissonClient, "redissonClient is null");
        RLock rLock = redissonClient.getLock(lockKey);
        try {
            rLock.lock();
            return callable.call();
        } catch (Exception e) {
            logger.error("redis lock error", e);
        } finally {
            rLock.unlock();
        }
        return null;
    }

    public static String getKey(String key) {
        Objects.requireNonNull(redissonClient, "redissonClient is null");
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

}
