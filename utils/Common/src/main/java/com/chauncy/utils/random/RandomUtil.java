package com.chauncy.utils.random;

import org.apache.commons.lang3.exception.UncheckedException;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * 随机工具类
 *
 * @author chauncy
 */
public class RandomUtil {

    /** ThreadLocalRandom实例 */
    public static RandomUtil INSECURE = new RandomUtil(ThreadLocalRandom::current);

    private static final ThreadLocal<SecureRandom> SECURE_RANDOM_STRONG = ThreadLocal.withInitial(() -> {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (final NoSuchAlgorithmException e) {
            throw new UncheckedException(e);
        }
    });
    private final static Supplier<Random> SECURE_RANDOM_SUPPLIER = SECURE_RANDOM_STRONG::get;

    /** SecureRandom实例 */
    public static RandomUtil SECURE = new RandomUtil(SECURE_RANDOM_SUPPLIER);

    private final Supplier<Random> randomSupplier;

    private RandomUtil(Supplier<Random> randomSupplier) {
        this.randomSupplier = randomSupplier;
    }

    private Random random() {
        return randomSupplier.get();
    }

    /**
     * 随机布尔值
     */
    public boolean randomBoolean() {
        return random().nextBoolean();
    }

    /**
     * 随机int, 0~exclusiveBound
     *
     * @param exclusiveBound 随机数最大值（不包含）
     */
    public int nextInt(int exclusiveBound) {
        return random().nextInt(exclusiveBound);
    }

    /**
     * 随机int, inclusiveMin~exclusiveMax
     *
     * @param inclusiveMin 随机数最小值（包含）
     * @param exclusiveMax 随机数最大值（不包含）
     */
    public int randomBound(int inclusiveMin, int exclusiveMax) {
        if (inclusiveMin > exclusiveMax) {
            throw new IllegalArgumentException("min must be less than max");
        }
        int bound = exclusiveMax - inclusiveMin;
        return inclusiveMin + random().nextInt(bound);
    }

    @Override
    public String toString() {
        return "RandomUtils [random=" + random() + "]";
    }
}
