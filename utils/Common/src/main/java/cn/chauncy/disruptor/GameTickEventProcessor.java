package cn.chauncy.disruptor;

import cn.chauncy.utils.time.TimeProvider;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ExceptionHandlerWrapper;

import java.util.concurrent.TimeUnit;

/**
 * 支持游戏 Tick 的 EventProcessor
 * <p>
 * 在事件处理过程中定期执行游戏 tick，保证：
 * 1. 每次处理的事件数量不超过上限
 * 2. 保证最低 30 FPS 的 tick 频率
 *
 * @param <T> 事件类型
 * @author chauncy
 */
public class GameTickEventProcessor<T> implements EventProcessor {

    /** 最低帧率目标：30 FPS */
    private static final long TARGET_FRAME_TIME_NANOS = TimeUnit.MILLISECONDS.toNanos(1000 / 30);

    /** 数据提供者，ring buffer，要求在其之上的等待策略必须是支持超时的 */
    private final DataProvider<T> dataProvider;
    /** 序列壁垒，此处是ring buffer生产者的游标 */
    private final SequenceBarrier barrier;
    /** 当前处理事件的游标 */
    private final Sequence sequence;
    /** 事件处理器 */
    private final EventHandler<? super T> handler;
    /** 主循环tick */
    private final GameTickProvider gameTickProvider;
    /** 异常处理器 */
    private final ExceptionHandlerWrapper<T> exceptionHandler = new ExceptionHandlerWrapper<>();


    /** 每帧处理事件最大数量 */
    private final int maxEventsPerTick;
    private volatile boolean running = false;

    public GameTickEventProcessor(DataProvider<T> dataProvider,
                                  SequenceBarrier barrier,
                                  EventHandler<? super T> handler,
                                  GameTickProvider gameTickProvider,
                                  int maxEventsPerTick) {
        this.dataProvider = dataProvider;
        this.barrier = barrier;
        this.handler = handler;
        this.maxEventsPerTick = maxEventsPerTick;
        this.gameTickProvider = gameTickProvider;
        this.sequence = new Sequence();
    }

    public void setExceptionHandler(ExceptionHandler<T> exceptionHandler) {
        this.exceptionHandler.switchTo(exceptionHandler);
    }

    @Override
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void run() {
        if (running) {
            throw new IllegalStateException("EventProcessor is already running");
        }

        running = true;
        barrier.clearAlert();

        notifyStart();

        try {
            processEvent();
        } catch (Exception e) {
            exceptionHandler.handleOnShutdownException(e);
        } finally {
            notifyShutdown();
        }

    }

    private void processEvent() throws Exception {
        T event = null;
        long nextSequence = sequence.get() + 1;

        while (running) {
            try {
                // 等待事件可用
                final long availableSequence = barrier.waitFor(nextSequence);
                if (availableSequence < nextSequence) {
                    gameTickProvider.tick();
                    continue;
                }

                final long endOfBatchSequence = Math.min(availableSequence, nextSequence + maxEventsPerTick);
                handler.onBatchStart(endOfBatchSequence - nextSequence + 1, availableSequence - nextSequence + 1);

                while (nextSequence < endOfBatchSequence) {
                    event = dataProvider.get(nextSequence);
                    handler.onEvent(event, nextSequence, false);
                    nextSequence++;
                }

                event = dataProvider.get(endOfBatchSequence);
                handler.onEvent(event, endOfBatchSequence, true);
                nextSequence++;
                sequence.set(nextSequence);

                gameTickProvider.tick();
            } catch (TimeoutException e) {
                notifyTimeout(sequence.get());
            } catch (Throwable t) {
                exceptionHandler.handleEventException(t, nextSequence, event);
                sequence.set(nextSequence);
                nextSequence++;
            }
        }
    }

    private void notifyStart() {
        try {
            handler.onStart();
        } catch (Exception e) {
            exceptionHandler.handleOnStartException(e);
        }
    }

    private void notifyShutdown() {
        try {
            handler.onShutdown();
        } catch (Exception e) {
            exceptionHandler.handleOnShutdownException(e);
        }
    }

    private void notifyTimeout(long sequence) {
        try {
            handler.onTimeout(sequence);
        } catch (Exception e) {
            exceptionHandler.handleOnShutdownException(e);
        }
    }

    @Override
    public void halt() {
        running = false;
        barrier.alert();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

}
