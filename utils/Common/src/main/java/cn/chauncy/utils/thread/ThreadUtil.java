package cn.chauncy.utils.thread;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 线程工具类
 *
 * @author chauncy
 */
public class ThreadUtil {

    /**
     * J9中新的的{@link StackWalker}，拥有更好的性能，因为它可以只创建需要的栈帧，而不是像异常一样总是获得所有栈帧的信息。
     */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Set.of(
            StackWalker.Option.SHOW_HIDDEN_FRAMES,
            StackWalker.Option.SHOW_REFLECT_FRAMES,
            StackWalker.Option.RETAIN_CLASS_REFERENCE));

    private static final AtomicInteger threadIndex = new AtomicInteger(1);

    private ThreadUtil() {}

    public static ThreadFactory createFactory(String name) {
        int index = threadIndex.incrementAndGet();
        return runnable -> new Thread(runnable, name + "-" + index);
    }

    /** 获取当前线程的名字 */
    public static String currentName() {
        return Thread.currentThread().getName();
    }

    /**
     * 清除当前线程的中断标志位
     */
    public static boolean clearInterrupt() {
        return Thread.interrupted();
    }

    /**
     * 检查当前线程的中断标志位, 如果为true，则抛出异常
     *
     * @throws InterruptedException 如果当前线程的中断标志位为true，则抛出异常
     */
    public static void checkInterrupt() throws InterruptedException{
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    public static void recoverInterrupted() {
        try {
            Thread.currentThread().interrupt();
        } catch (SecurityException e) {
            // ignore
        }
    }

    /**
     * 恢复中断异常, 如果异常是InterruptedException，则恢复中断状态, 否则不处理
     *
     * @param t 异常
     */
    public static void recoverInterrupted(Throwable t) {
        if (t instanceof InterruptedException) {
            try {
                Thread.currentThread().interrupt();
            } catch (SecurityException e) {
                // ignore
            }
        }
    }

    /**
     * 试图安静地线程休眠指定的毫秒数，不抛出异常
     * <p>
     * 如果中断，会提前终止睡眠状态，且恢复中断标志位
     * 特别注意不要在循环或递归中调用此方法，如果遇到中断会立即返回，可能有堆栈溢出的风险
     *
     * @param millis 休眠时间
     */
    public static void sleeplessQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 强制安静地线程休眠指定的毫秒数，不抛出异常
     * <p>
     * 如果中断，不会提前终止睡眠状态，且在结束休眠后恢复中断标志位
     *
     * @param millis 休眠时间
     */
    public static void sleepForceQuietly(long millis) {
        final long deadline = System.currentTimeMillis() + millis;

        long remaining = millis;
        boolean interrupted = false;
        while (remaining > 0) {
            try {
                Thread.sleep(remaining);
                break;
            } catch (InterruptedException e) {
                remaining = deadline - System.currentTimeMillis();
                interrupted = true;
            }
        }

        if (interrupted) {
            recoverInterrupted();
        }
    }

    /**
     * 线程join，不抛出异常, 如果被中断，则恢复中断状态
     *
     * @param thread 线程
     */
    public static void joinUnInterrupted(Thread thread) {
        boolean isInterrupted = false;
        while (true) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                isInterrupted = true;
            }
        }

        if (isInterrupted) {
            recoverInterrupted();
        }
    }

    /**
     * 获取调用栈信息
     *
     * @param deep 调用栈深度
     * @return 调用栈信息
     */
    public static String getCallerInfo(int deep) {
        String result = STACK_WALKER.walk(frames -> frames
                .limit(deep + 1)
                .filter(stackFrame -> stackFrame.getDeclaringClass() != ThreadUtil.class)
                .map(StackWalker.StackFrame::toString)
                .collect(Collectors.joining("\n"))
        );
        return StringUtils.isEmpty(result) ? "No stack information" : result;
    }

}
