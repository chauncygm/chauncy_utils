package com.chauncy.utils.eventbus;

import com.chauncy.utils.thread.ThreadUtil;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * 事件派发器，参考自guava的事件派发器
 * 需要注意循环触发的事件可能导致死循环
 * 如： A->A->A->...
 *     A->B->C->A->...
 */
public abstract class Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private Dispatcher() {}

    /** 可重入的立即派发的派发器（深度优先） */
    static final Dispatcher Immediate = new ImmediateDispatcher();

    /** 加入线程私有队列，根据加入顺序进行派发（广度优先） */
    static final Dispatcher PerThreadQueue = new PerThreadQueueDispatcher();

    /** 加入全局队列，根据加入顺序进行派发，适用于异步执行器 */
    static final Dispatcher LegacyAsync = new LegacyAsyncDispatcher();

    /**
     * 派发事件
     *
     * @param executor      执行器
     * @param event         事件
     * @param subscriber    订阅者
     */
    abstract <T> void dispatch(Executor executor, Object event, Subscriber<?> subscriber);

    /**
     * 安全地处理事件
     *
     * @param executor      执行器
     * @param eventWithSubscriber   事件和订阅者
     */
    private static <T> void selfHandlerEvent(Executor executor, EventWithSubscriber<T> eventWithSubscriber) {
        executor.execute(() -> {
            try {
                Object event = eventWithSubscriber.event;
                Subscriber<T> subscribers = eventWithSubscriber.subscribers;
                //noinspection unchecked
                subscribers.onEvent((T) event);
            } catch (Exception e) {
                logger.error("execute subscribers handle event error", e);
            }
        });
    }

    private static class ImmediateDispatcher extends Dispatcher {
        /** 最大递归深度 */
        private static final int MAX_RECURSION_DEPTH = 16;
        /** 递归循环深度，线程私有，如果执行器时异步的则无需考虑 */
        private final ThreadLocal<Integer> recursionDepth = ThreadLocal.withInitial(() -> 0);

        @Override
        <T> void dispatch(Executor executor, Object event, Subscriber<?> subscriber) {
            if (recursionDepth.get() > MAX_RECURSION_DEPTH) {
                logger.error("Recursion depth exceeded, stacktrace: {}", ThreadUtil.getCallerInfo(16));
                return;
            }

            Integer depth = recursionDepth.get();
            recursionDepth.set(depth + 1);
            try {
                selfHandlerEvent(executor, new EventWithSubscriber<>(event, subscriber));
            } finally {
                recursionDepth.set(depth);
            }
        }
    }

    private static class PerThreadQueueDispatcher extends Dispatcher {

        private final ThreadLocal<Queue<EventWithSubscriber<?>>> queue = ThreadLocal.withInitial(Queues::newArrayDeque);
        private final ThreadLocal<Boolean> dispatching = ThreadLocal.withInitial(() -> false);

        @Override
        <T> void dispatch(Executor executor, Object event, Subscriber<?> subscriber) {
            queue.get().add(new EventWithSubscriber<>(event, subscriber));
            if (!dispatching.get()) {
                dispatching.set(true);
                try {
                    while (!queue.get().isEmpty()) {
                        EventWithSubscriber<?> e = queue.get().poll();
                        selfHandlerEvent(executor, e);
                    }
                } finally {
                    dispatching.remove();
                    queue.remove();
                }
            }
        }
    }

    private static class LegacyAsyncDispatcher extends Dispatcher {

        private final Queue<EventWithSubscriber<?>> queue = Queues.newConcurrentLinkedQueue();

        @Override
        <T> void dispatch(Executor executor, Object event, Subscriber<?> subscriber) {
            queue.offer(new EventWithSubscriber<>(event, subscriber));
            EventWithSubscriber<?> e;
            while ((e = queue.poll()) != null) {
                selfHandlerEvent(executor, e);
            }
        }
    }

    private record EventWithSubscriber<T>(Object event, Subscriber<T> subscribers) {}

}

