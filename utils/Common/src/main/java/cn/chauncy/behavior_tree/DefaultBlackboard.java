package cn.chauncy.behavior_tree;

import io.netty.util.AttributeKey;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.IdentityHashMap;
import java.util.Map;

public class DefaultBlackboard implements Blackboard {

    public static final AttributeKey<Object> ENTITY_KEY = AttributeKey.valueOf("entity");

    private final Map<AttributeKey<?>, Object> map = new IdentityHashMap<>();

    @Override
    public <T> T get(@NonNull AttributeKey<T> key) {
        //noinspection unchecked
        return (T) map.get(key);
    }

    @Override
    public <T> void set(@NonNull AttributeKey<T> key, @NonNull T value) {
        map.put(key, value);
    }

    @Override
    public <T> boolean containsKey(@NonNull AttributeKey<T> key) {
        return map.containsKey(key);
    }

    @Override
    public <T> void remove(@NonNull AttributeKey<T> key) {
        map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void reset() {
        map.clear();
    }
}
