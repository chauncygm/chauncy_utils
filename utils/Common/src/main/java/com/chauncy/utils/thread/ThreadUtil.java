package com.chauncy.utils.thread;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;
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

    private ThreadUtil() {}

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
     * 安静地线程休眠，不抛出异常
     *
     * @param millis 休眠时间
     */
    public static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
