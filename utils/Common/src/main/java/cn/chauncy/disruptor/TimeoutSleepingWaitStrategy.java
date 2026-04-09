package cn.chauncy.disruptor;

import com.lmax.disruptor.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 游戏主循环 WaitStrategy 实现
 * <p>
 * 在 Disruptor 中拉取事件最多等待33.3ms，保证帧率不低于30FPS
 * <p>
 * 没有事件时，最多等待33.3ms，保证最低 30 FPS（每帧不超过 33.33ms）
 *
 * @author chauncy
 */
public class TimeoutSleepingWaitStrategy implements TimeoutWaitStrategy {

    /**
     * 最低帧率目标：30 FPS (33.33ms)
     */
    private static final long TARGET_FRAME_TIME_NANOS = TimeUnit.MILLISECONDS.toNanos(1000 / 30);

    private static final int SPIN_COUNT = 10;
    private static final int YIELD_COUNT = 10;
    private static final int SLEEP_RETIES = 10;
    private static final int SLEEPING_NANOS = 100_000;

    private final int spinCount;
    private final int yieldCount;
    private final int sleepRetries;
    private final long sleepNanos;

    public TimeoutSleepingWaitStrategy() {
        this(SPIN_COUNT, YIELD_COUNT, SLEEP_RETIES, SLEEPING_NANOS);
    }

    public TimeoutSleepingWaitStrategy(int spinCount, int yieldCount, int sleepRetries, int sleepNanos) {
        this.spinCount = spinCount;
        this.yieldCount = yieldCount;
        this.sleepRetries = sleepRetries;
        this.sleepNanos = sleepNanos;
    }

    @Override
    public long waitFor(long sequence, Sequence cursor, Sequence dependentSequence, SequenceBarrier barrier) throws AlertException {
        int counter = spinCount + yieldCount + sleepRetries;
        int yieldThreshold = sleepRetries + yieldCount;
        long parkDeadline = System.nanoTime() + sleepNanos * sleepRetries;

        long availableSequence;
        while ((availableSequence = dependentSequence.get()) < sequence) {
            barrier.checkAlert();

            if (counter > yieldThreshold) {
                counter--;
                Thread.onSpinWait();
                continue;
            }
            if (counter > sleepRetries) {
                counter--;
                Thread.yield();
                continue;
            }

            if (counter > 0) {
                counter--;
                long remainNanos = parkDeadline - System.nanoTime();
                if (remainNanos > 0) {
                    LockSupport.parkNanos(remainNanos);
                } else {
                    return sequence - 1;
                }
            } else {
                return sequence - 1;
            }
        }
        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking() {
        // do nothing
    }


}
