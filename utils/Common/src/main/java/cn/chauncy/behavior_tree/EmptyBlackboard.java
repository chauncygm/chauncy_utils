package cn.chauncy.behavior_tree;

import io.netty.util.AttributeKey;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EmptyBlackboard implements Blackboard {

    @Override
    public <T> T get(@NonNull AttributeKey<T> key) {
        return null;
    }

    @Override
    public <T> void set(@NonNull AttributeKey<T> key, @NonNull T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> boolean containsKey(@NonNull AttributeKey<T> key) {
        return false;
    }

    @Override
    public <T> void remove(@NonNull AttributeKey<T> key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void reset() {
    }
}
