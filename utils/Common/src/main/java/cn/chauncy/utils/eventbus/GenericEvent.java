package cn.chauncy.utils.eventbus;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * 泛型事件
 */
public interface GenericEvent {

    @NonNull Class<?> getEventType();

}
