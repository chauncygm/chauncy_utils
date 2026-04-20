package cn.chauncy.behavior_tree;

import io.netty.util.AttributeKey;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Blackboard {

    <T> T get(@NonNull AttributeKey<T> key);

    <T> void set(@NonNull AttributeKey<T> key, @NonNull T value);

    default <T> T getOrDefault(@NonNull AttributeKey<T> key, @NonNull T defaultValue) {
        final T value = get(key);
        if (value != null || containsKey(key)) {
            return value;
        }
        return defaultValue;
    }

    default <T> T getOrElse(@NonNull AttributeKey<T> key, @NonNull T other) {
        T value = get(key);
        return value != null ? value : other;
    }

    <T> boolean containsKey(@NonNull AttributeKey<T> key);

    <T> void remove(@NonNull AttributeKey<T> key);

    int size();

    boolean isEmpty();

    void reset();
}
