package cn.chauncy.utils.eventbus;

import io.netty.util.internal.shaded.org.jctools.queues.unpadded.MpscUnpaddedArrayQueue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * 事件收集器
 * 集中处理过程中产生的事件
 */
public class EventCollectors implements EventBus {

    private static final Logger logger = LoggerFactory.getLogger(EventCollectors.class);
    private final ThreadLocal<Boolean> readyState = ThreadLocal.withInitial(() -> false);

    private final EventBus delegate;
    private final Queue<Object> eventQueue = new MpscUnpaddedArrayQueue<>(1024);

    public EventCollectors(EventBus delegate) {
        this.delegate = delegate;
    }

    @Override
    public void post(@NonNull Object event) {
        Boolean ready = readyState.get();
        if (!ready) {
            eventQueue.add(event);
        } else {
            delegate.post(event);
        }
    }

    public void postImmediate(@NonNull Object event) {
        delegate.post(event);
    }

    public void init() {
        eventQueue.clear();
        readyState.set(false);
    }

    public void ready() {
        readyState.set(true);
        while (!eventQueue.isEmpty()) {
            try {
                delegate.post(eventQueue.poll());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        readyState.remove();
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
