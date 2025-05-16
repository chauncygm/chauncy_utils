package com.chauncy.utils.eventbus;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 组合订阅者，装饰器模式
 * 表示一或多个订阅者，组合同一类的订阅者
 */
@SuppressWarnings("unchecked")
public abstract class ComposeSubscriber<T> implements Subscriber<T> {

    private static final Logger log = LoggerFactory.getLogger(ComposeSubscriber.class);

    public static final ComposeSubscriber<?> EMPTY_SUBSCRIBER = new EmptySubscriber<>();

    /** 组合订阅者 */
    public abstract ComposeSubscriber<?> compose(@NonNull Subscriber<?>... handler);

    /** 剔除订阅者 */
    public abstract void decompose(@NonNull Subscriber<?> handler);

    public abstract int size();

    public boolean isEmpty() {
        return size() == 0;
    }


    public static <T> ComposeSubscriber<T> create(Subscriber<T> subscriber) {
        return new SingleSubscriber<>(subscriber);
    }

    public static <T> ComposeSubscriber<T> create(Subscriber<T>... subscribers) {
        return new MultiSubscriber<>(subscribers);
    }

    private static <T> void safeHandleEvent(Subscriber<?> subscriber, T event) {
        try {
            Subscriber<T> tSubscriber = (Subscriber<T>) subscriber;
            tSubscriber.onEvent(event);
        } catch (Exception e) {
            String handlerClassName = subscriber.getClass().getName();
            String eventClassName = event.getClass().getName();
            log.error("handle events error, subscriber: {}, event: {}", handlerClassName, eventClassName, e);
        }
    }

    //region ComposeSubscriber实现类
    private static class EmptySubscriber<T> extends ComposeSubscriber<T> {
        @Override
        public ComposeSubscriber<T> compose(Subscriber<?>... handler) {
            if (handler.length == 1) {
                return new SingleSubscriber<>(handler[0]);
            } else {
                return new MultiSubscriber<>(handler);
            }
        }

        @Override
        public void decompose(@NonNull Subscriber<?> handler) {}

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void onEvent(@NonNull T event) {
            //  do nothing
        }
    }

    private static class SingleSubscriber<T> extends ComposeSubscriber<T>{
        private Subscriber<?> subscriber;

        public SingleSubscriber(Subscriber<?> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public ComposeSubscriber<?> compose(Subscriber<?>... handler) {
            return new MultiSubscriber<>(subscriber).compose(handler);
        }

        @Override
        public void decompose(@NonNull Subscriber<?> handler) {
            if (this.subscriber == handler) {
                this.subscriber = null;
            }
        }

        @Override
        public int size() {
            return subscriber == null ? 0 : 1;
        }

        @Override
        public void onEvent(@NonNull T event) {
            if (subscriber == null) {
                throw new IllegalStateException("subscriber is null");
            }
            safeHandleEvent(subscriber, event);
        }
    }

    private static class MultiSubscriber<T> extends ComposeSubscriber<T> {
        private final List<Subscriber<?>> subscriberList = new ArrayList<>(4);

        public MultiSubscriber(Subscriber<T> subscriber) {
            subscriberList.add(subscriber);
        }

        public MultiSubscriber(Subscriber<?>... subscriber) {
            Collections.addAll(subscriberList, subscriber);
        }

        @Override
        public ComposeSubscriber<T> compose(Subscriber<?>... handler) {
            Collections.addAll(subscriberList, handler);
            return this;
        }

        @Override
        public void decompose(@NonNull Subscriber<?> handler) {
            for (int i = 0; i < subscriberList.size(); i++) {
                if (subscriberList.get(i) == handler) {
                    subscriberList.remove(i);
                    break;
                }
            }
        }

        @Override
        public int size() {
            return subscriberList.size();
        }

        @Override
        public void onEvent(@NonNull T event) {
            for (Subscriber<?> subscriber : subscriberList) {
                safeHandleEvent(subscriber, event);
            }
        }
    }
    //endregion

}
