package com.chauncy.utils.eventbus;

import com.google.common.base.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.chauncy.utils.eventbus.ComposeSubscriber.EMPTY_SUBSCRIBER;

@NotThreadSafe
public class DefaultEventBus implements EventBus {

    private static final Executor CURRENT_THREAD = Runnable::run;

    /** 缓存事件类型的key */
    private final ArrayList<ComposeKey> keyPool = new ArrayList<>(16);
    /** 记录事件类型对应的处理器集合 */
    private final Map<Object, ComposeSubscriber<?>> handlerMap = new HashMap<>();

    /** 处理事件的线程执行器 */
    private final Executor executor;
    /** 事件派发器 */
    private final Dispatcher dispatcher;

    public DefaultEventBus() {
        this(false);
    }

    public DefaultEventBus(boolean immediate) {
        this(CURRENT_THREAD, immediate ?  Dispatcher.Immediate : Dispatcher.PerThreadQueue);
    }

    public DefaultEventBus(Executor executor, Dispatcher dispatcher) {
        this.executor = executor;
        this.dispatcher = dispatcher;
    }

    @Override
    public void post(@NonNull Object event) {
        ComposeSubscriber<?> subscribers = getSubscribers(event);
        if (!subscribers.isEmpty()) {
            dispatcher.dispatch(executor, event, subscribers);
        }
    }

    @Override
    public void register(@NonNull Subscriber<?> handler) {
        Type eventType = getEventHandlerGenericType(handler);
        if (eventType == null) {
            throw new IllegalArgumentException("EventHandler must have a generic type");
        }
        ComposeSubscriber<?> eventHandlers = handlerMap.getOrDefault(eventType, EMPTY_SUBSCRIBER);
        handlerMap.put(eventType, eventHandlers.compose(handler));
    }

    @Override
    public void unregister(@NonNull Subscriber<?> handler) {
        Type eventType = getEventHandlerGenericType(handler);
        if (eventType == null) {
            throw new IllegalArgumentException("EventHandler must have a generic type");
        }
        ComposeSubscriber<?> composeSubscriber = handlerMap.getOrDefault(eventType, EMPTY_SUBSCRIBER);
        if (!composeSubscriber.isEmpty()) {
            composeSubscriber.decompose(handler);
        }
        if (composeSubscriber.isEmpty()) {
            handlerMap.remove(eventType);
        }
    }

    @Override
    public <T> void register(@NonNull Class<T> masterKey, @Nullable Class<?> subKey, @NonNull Subscriber<? extends T> handler) {
        Object key = subKey == null ? masterKey : acquireKey(masterKey, subKey);
        ComposeSubscriber<?> eventHandlers = handlerMap.getOrDefault(key, EMPTY_SUBSCRIBER);
        handlerMap.put(key, eventHandlers.compose(handler));
    }

    @Override
    public <T> void unregister(@NonNull Class<T> masterKey, @Nullable Class<?> subKey, @NonNull Subscriber<? extends T> handler) {
        Object key = subKey == null ? masterKey : acquireKey(masterKey, subKey);
        ComposeSubscriber<?> composeSubscriber = handlerMap.getOrDefault(key, EMPTY_SUBSCRIBER);
        if (!composeSubscriber.isEmpty()) {
            composeSubscriber.decompose(handler);
        }
        if (composeSubscriber.isEmpty()) {
            handlerMap.remove(key);
        }
        if (subKey != null) {
            releaseKey((ComposeKey) key);
        }
    }

    private ComposeSubscriber<?> getSubscribers(Object event) {
        ComposeSubscriber<?> subscribers;
        if (event instanceof GenericEvent genericEvent) {
            ComposeKey composeKey = acquireKey(event.getClass(), genericEvent.getEventType());
            subscribers = handlerMap.get(composeKey);
            releaseKey(composeKey);
        } else {
            subscribers = handlerMap.get(event.getClass());
        }
        return subscribers == null ? EMPTY_SUBSCRIBER : subscribers;
    }

    private static Type getEventHandlerGenericType(Subscriber<?> handler) {
        Type eventType = null;
        Class<?> aClass = handler.getClass();
        Type[] genericSuperclass = aClass.getGenericInterfaces();
        for (Type superclassType : genericSuperclass) {
            if (superclassType instanceof ParameterizedType parameterizedType) {
                Class<?> rawInterface = (Class<?>) parameterizedType.getRawType();

                // 确保是 EventHandler 接口，并且有泛型参数
                if (Subscriber.class.isAssignableFrom(rawInterface)) {
                    eventType = parameterizedType.getActualTypeArguments()[0];
                    break;
                }
            }
        }
        return eventType;
    }

    private ComposeKey acquireKey(Class<?> masterKey, Class<?> subKey) {
        ComposeKey composeKey = keyPool.isEmpty() ? new ComposeKey() : keyPool.remove(keyPool.size() - 1);
        return composeKey.init(masterKey, subKey);
    }

    private void releaseKey(ComposeKey key) {
        key.reset();
        keyPool.add(key);
    }

    static class ComposeKey {
        private Class<?> masterKey;
        private Class<?> subKey;

        public ComposeKey() {}

        public ComposeKey(Class<?> masterKey, Class<?> subKey) {
            this.masterKey = masterKey;
            this.subKey = subKey;
        }

        private ComposeKey init(Class<?> masterKey, Class<?> subKey) {
            this.masterKey = masterKey;
            this.subKey = subKey;
            return this;
        }

        private void reset() {
            masterKey = null;
            subKey = null;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ComposeKey that = (ComposeKey) o;
            return Objects.equal(masterKey, that.masterKey) && Objects.equal(subKey, that.subKey);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(masterKey, subKey);
        }
    }

}
