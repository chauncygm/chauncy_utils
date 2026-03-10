package cn.chauncy.utils.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultThreadFactory.class);

    private static final AtomicInteger poolId = new AtomicInteger();
    private static final Thread.UncaughtExceptionHandler DEFAULT_HANDLER =
            (t, e) -> logger.error("thead exit due to uncaught exception, theadName: {}", t.getName(),  e);

    private final String prefix;
    private final int priority;
    private final boolean daemon;
    private final AtomicInteger nextId = new AtomicInteger();
    private Thread.UncaughtExceptionHandler uncaughtExHandler;

    public DefaultThreadFactory(String prefix) {
        this(prefix, Thread.NORM_PRIORITY, false);
    }

    public DefaultThreadFactory(String prefix, int priority) {
        this(prefix, priority, false);
    }

    public DefaultThreadFactory(String prefix, boolean daemon) {
        this(prefix, Thread.NORM_PRIORITY, daemon);
    }

    public DefaultThreadFactory(String prefix, int priority, boolean daemon) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("prefix cannot be null or empty");
        }
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("priority must be between " + Thread.MIN_PRIORITY + " and " + Thread.MAX_PRIORITY);
        }
        this.prefix = prefix + "-" + poolId.getAndIncrement() + "-";
        this.priority = priority;
        this.daemon = daemon;
    }

    public void setUncaughtExHandler(Thread.UncaughtExceptionHandler uncaughtExHandler) {
        this.uncaughtExHandler = uncaughtExHandler;
    }


    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, prefix + "-" + nextId.getAndIncrement());
        t.setPriority(priority);
        t.setDaemon(daemon);
        t.setUncaughtExceptionHandler(uncaughtExHandler == null ? DEFAULT_HANDLER : uncaughtExHandler);
        return t;
    }
}
