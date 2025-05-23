package cn.chauncy.utils.eventbus;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface Subscriber<T> {

    void onEvent(@NonNull T event) throws Exception;

}
