package com.chauncy.utils.eventbus;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * 事件总线
 */
public interface EventBus {

    /**
     * 发布事件
     * @param event 事件
     */
    void post(@NonNull Object event);

    /**
     * 注册事件处理
     * @param handler 处理器
     */
    void register(@NonNull Subscriber<?> handler);

    /**
     * 注销事件处理
     * @param handler 处理器
     */
    void unregister(@NonNull Subscriber<?> handler);

    /**
     * 注册事件处理器，适用于泛型事件
     * @param masterKey 主键
     * @param subKey    子键
     * @param handler   处理器
     */
    <T> void register(@NonNull Class<T> masterKey, @Nullable Class<?> subKey, @NonNull Subscriber<? extends T> handler);

    /**
     * 注销事件处理器，适用于泛型事件
     * @param masterKey 主键
     * @param subKey    子键
     * @param handler   处理器
     */
    <T> void unregister(@NonNull Class<T> masterKey, @Nullable Class<?> subKey, @NonNull Subscriber<? extends T> handler);
}
