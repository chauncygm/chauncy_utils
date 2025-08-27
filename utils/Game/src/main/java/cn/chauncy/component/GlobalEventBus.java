package cn.chauncy.component;

import cn.chauncy.utils.eventbus.DefaultEventBus;
import cn.chauncy.utils.eventbus.EventBus;
import cn.chauncy.utils.eventbus.Subscriber;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class GlobalEventBus implements EventBus {

    private final EventBus delegate = new DefaultEventBus(true);

    @Override
    public void post(@NonNull Object event) {
        delegate.post(event);
    }

    @Override
    public void register(@NonNull Subscriber<?> handler) {
        delegate.register(handler);
    }

    @Override
    public void unregister(@NonNull Subscriber<?> handler) {
        delegate.unregister(handler);
    }

    @Override
    public <T> void register(@NonNull Class<T> masterKey, @Nullable Class<?> subKey, @NonNull Subscriber<? extends T> handler) {
        delegate.register(masterKey, subKey, handler);
    }

    @Override
    public <T> void unregister(@NonNull Class<T> masterKey, @Nullable Class<?> subKey, @NonNull Subscriber<? extends T> handler) {
        delegate.unregister(masterKey, subKey, handler);
    }
}
