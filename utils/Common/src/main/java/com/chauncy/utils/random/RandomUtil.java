package com.chauncy.utils.random;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.concurrent.ThreadSafe;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * 随机工具类
 *
 * @author chauncy
 */
@ThreadSafe
public class RandomUtil {

    public static final ThreadLocal<Random> LOCAL_RANDOM = ThreadLocal.withInitial(Random::new);

    private static final ThreadLocal<SecureRandom> SECURE_RANDOM_STRONG = ThreadLocal.withInitial(() -> {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    });

    /** Random实例 */
    public static RandomUtil INSECURE = new RandomUtil(LOCAL_RANDOM::get);

    /** SecureRandom实例 */
    public static RandomUtil SECURE = new RandomUtil(SECURE_RANDOM_STRONG::get);

    private final Supplier<Random> randomSupplier;

    private RandomUtil(Supplier<Random> randomSupplier) {
        this.randomSupplier = randomSupplier;
    }

    private Random random() {
        return randomSupplier.get();
    }

    /**
     * 设置种子，注意仅在当前线程下生效，在当前线程下是单例
     * 仅对INSECURE实例生效
     *
     * @param seed 种子值
     */
    public void setSeed(long seed) {
        if (this == SECURE) {
            throw new UnsupportedOperationException("SecureRandom not support set seed.");
        }
        random().setSeed(seed);
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
        if (exclusiveBound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
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
        return inclusiveMin + nextInt(bound);
    }

    /**
     * 集合中随机一项
     *
     * @param items   随机项集合
     * @return 随机项
     */
    public <Item> Optional<Item> random(@NonNull Collection<Item> items) {
        if (items.isEmpty()) {
            return Optional.empty();
        }
        if (items instanceof List) {
            return random((List<Item>) items);
        }
        List<Item> temp = new ArrayList<>(items);
        return random(temp);
    }

    /**
     * 集合中随机一项
     *
     * @param items   随机项列表
     * @return 随机项
     */
    public <Item> Optional<Item> random(@NonNull List<Item> items) {
        if (items.isEmpty()) {
            return Optional.empty();
        }

        int randomIndex = nextInt(items.size());
        return Optional.of(items.get(randomIndex));
    }

    /**
     * 集合中随机多项(可重复)
     *
     * @param items   随机项列表
     * @param num     随机数量
     * @return 随机项列表
     */
    public <Item> List<Item> randomList(@NonNull List<Item> items, int num) {
        if (items.isEmpty() || num <= 0) {
            return List.of();
        }

        List<Item> result = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            int randomIndex = nextInt(items.size());
            result.add(items.get(randomIndex));
        }
        return result;
    }

    /**
     * 集合中随机多项(不可重复)
     * Fisher-Yates洗牌算法
     *
     * @param items   随机项列表
     * @param num     随机数量
     * @throws IllegalArgumentException 数量大于集合提供的数量
     * @return 随机项列表
     */
    public <Item> List<Item> randomSoleList(@NonNull Collection<Item> items, int num) {
        if (items.isEmpty() || num <= 0) {
            return List.of();
        }

        if (num > items.size()) {
            throw new IllegalArgumentException("num exceeds items.size()");
        }

        List<Item> copy = new ArrayList<>(items);
        for (int i = 0; i < num; i++) {
            //这里需要允许自己和自己交换
            int swapIndex = i + nextInt(copy.size() - i);
            Collections.swap(copy, i, swapIndex);
        }
        return num == items.size() ? copy : copy.subList(0, num);
    }

    /**
     * 随机权重
     *
     * @param items   随机项
     * @param weightFunction  随机项权重
     * @throws IllegalArgumentException 随机项权重小于0
     * @return 随机项
     */
    public <Item> Optional<Item> randomWeight(@NonNull Collection<Item> items, @NonNull ToIntFunction<Item> weightFunction) {
        WeightData<Item> itemWeightData = prepareWeightData(items, weightFunction);
        List<Item> itemList = itemWeightData.items();
        int total = itemWeightData.total();
        int[] prefixSum = itemWeightData.prefixSum();

        if (total == 0) {
            return Optional.empty();
        }
        int randomValue = nextInt(total) + 1;
        int idx = Arrays.binarySearch(prefixSum, randomValue);
        Item item = itemList.get(idx >= 0 ? idx : -idx - 1);
        return Optional.of(item);
    }

    /**
     * 随机权重列表
     *
     * @param items   随机项
     * @param weightFunction  随机项权重
     * @param num     随机数量
     * @return 随机项列表
     */
    public <Item> List<Item> randomWeightList(@NonNull Collection<Item> items, @NonNull ToIntFunction<Item> weightFunction, int num) {
        WeightData<Item> itemWeightData = prepareWeightData(items, weightFunction);
        List<Item> itemList = itemWeightData.items();
        int total = itemWeightData.total();
        int[] prefixSum = itemWeightData.prefixSum();

        List<Item> result = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            int randomValue = nextInt(total) + 1;
            int idx = Arrays.binarySearch(prefixSum, randomValue);
            Item item = itemList.get(idx >= 0 ? idx : -idx - 1);
            result.add(item);
        }
        return result;
    }

    /** 公共权重数据结构 */
    private record WeightData<Item>(List<Item> items, int[] prefixSum, int total) {}

    /**
     * 解析权重数据
     *
     * @param items 权重随机集合
     * @param weight 获取权重
     * @return  权重数据
     */
    private <Item> WeightData<Item> prepareWeightData(Collection<Item> items, ToIntFunction<Item> weight) {
        List<Item> itemList = new ArrayList<>(items);
        int total = 0;
        int[] prefixSum = new int[itemList.size()];
        for (int i = 0; i < itemList.size(); i++) {
            int w = weight.applyAsInt(itemList.get(i));
            if (w < 0) {
                throw new IllegalArgumentException("weight num must be positive");
            }
            total += w;
            prefixSum[i] = total;
        }
        return new WeightData<>(itemList, prefixSum, total);
    }

    @Override
    public String toString() {
        return "RandomUtils [random=" + random() + "]";
    }
}
