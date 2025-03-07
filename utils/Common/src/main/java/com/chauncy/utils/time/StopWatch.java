package com.chauncy.utils.time;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.chauncy.utils.time.TimeUtils.NANOS_PER_MILLIS;

/**
 * 计时器(停表)，用于记录程序运行时间
 *
 * @author chauncy
 */
@NotThreadSafe
public class StopWatch {

    /** 计时器名字 */
    private final String name;

    /** 运行状态 */
    private State state = State.UNSTART;

    /** 启动时间 */
    private long startNanos;

    /** 总计时 */
    private long elapsedNanos;

    /** 当前步骤计时 */
    private long stepElapsedNanos;

    /** 停止时间 */
    private long stopNanos;

    /** 打点记录 */
    private List<Record> records = new ArrayList<>();

    public StopWatch(String name) {
        this.name = name;
    }

    /** 创建并启动一个停表 */
    public static StopWatch create(String name) {
        StopWatch stopWatch = new StopWatch(name);
        stopWatch.start();
        return stopWatch;
    }

    /** 获取停表的名字 */
    public String getName() {
        return name;
    }

    //region 生命周期
    /** 启动停表，开始计时 */
    public void start() {
        if (isStarted()) {
            throw new IllegalStateException("StopWatch already started");
        }
        this.startNanos = System.nanoTime();
        this.state = State.RUNNING;
        this.records.clear();
    }

    /**
     * 记录当前步骤的耗时，并开始新的计时
     *
     * @param stepName 步骤名称
     */
    public void logStep(@NonNull String stepName) {
        Objects.requireNonNull(stepName, "stepName is null");
        if (!isRunning()) {
            throw new IllegalStateException("StopWatch is not running");
        }
        long delta = System.nanoTime() - startNanos;
        this.startNanos += delta;
        this.elapsedNanos += delta;
        this.stepElapsedNanos += delta;

        this.records.add(new Record(stepName, stepElapsedNanos));
        this.stepElapsedNanos = 0;
    }

    /** 挂起/暂停计时器 */
    public void suspend() {
        if (!isStarted()) {
            throw new IllegalStateException("StopWatch is not started: " + name);
        }
        if (isRunning()) {
            long delta = System.nanoTime() - startNanos;
            this.startNanos += delta;
            this.elapsedNanos += delta;
            this.stepElapsedNanos += delta;
            this.state = State.SUSPENDED;
        }
    }

    /** 恢复计时器 */
    public void resume() {
        if (!isStarted()) {
            throw new IllegalStateException("StopWatch is not started: " + name);
        }
        if (isSuspended()) {
            this.startNanos = System.nanoTime();
            this.state = State.RUNNING;
        }
    }

    /** 停止计时器 */
    public void stop() {
        stop(null);
    }

    /**
     * 停止计时器，并记录当前步骤的耗时
     *
     * @param stepName 步骤名称
     */
    public void stop(@Nullable String stepName) {
        if (!isStarted()) {
            throw new IllegalStateException("StopWatch is not started: " + name);
        }
        if (isRunning()) {
            long delta = System.nanoTime() - startNanos;
            this.startNanos += delta;
            this.stepElapsedNanos += delta;
            this.elapsedNanos += delta;
            if (stepName != null) {
                this.records.add(new Record(stepName, stepElapsedNanos));
            }
            this.stepElapsedNanos = 0;
        }
        this.state = State.STOPPED;
    }

    /** 重置计时器 */
    public void reset() {
        this.state = State.UNSTART;
        this.startNanos = 0;
        this.elapsedNanos = 0;
        this.stepElapsedNanos = 0;
        this.records.clear();
    }

    /** 重置计时器，并重新开始计时 */
    public void restart() {
        reset();
        start();
    }
    //endregion

    //region 查询耗时
    /** 获取总耗时 */
    public Duration elapsed() {
        return Duration.ofNanos(elapsedNanos());
    }

    /**
     * 获取总耗时
     * @param desiredUnit 时间单位
     */
    public long elapsed(TimeUnit desiredUnit) {
        return desiredUnit.convert(elapsedNanos(), TimeUnit.NANOSECONDS);
    }

    /** 获取当前步骤耗时 */
    public Duration stepElapsed() {
        return Duration.ofNanos(stepElapsedNanos());
    }

    /**
     * 获取当前步骤耗时
     * @param desiredUnit 时间单位
     */
    public long stepElapsed(TimeUnit desiredUnit) {
        return desiredUnit.convert(stepElapsedNanos(), TimeUnit.NANOSECONDS);
    }

    /** 获取所有步骤耗时 */
    public List<Map.Entry<String, Duration>> listStepElapsed() {
        List<Map.Entry<String, Duration>> result = new ArrayList<>(records.size());
        for (Record record : records) {
            result.add(Map.entry(record.stepName, Duration.ofNanos(record.elapsedNanos)));
        }
        return result;
    }

    private long elapsedNanos() {
        if (isRunning()) {
            long delta = System.nanoTime() - startNanos;
            return elapsedNanos + delta;
        }
        return elapsedNanos;
    }

    private long stepElapsedNanos() {
        if (isRunning()) {
            long delta = System.nanoTime() - startNanos;
            return stepElapsedNanos + delta;
        }
        return stepElapsedNanos;
    }
    //endregion

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[StopWatch ").append(name).append("=").append(elapsedNanos / NANOS_PER_MILLIS).append("ms]\n");
        sb.append('{');
        for (Record record : records) {
            sb.append(record.toString()).append(',');
        }
        if (!records.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * 步骤耗时记录
     */
    record Record(String stepName, long elapsedNanos) implements Comparable<Record> {

        @Override
            public int compareTo(Record o) {
                int compare = Long.compare(elapsedNanos, o.elapsedNanos);
                if (compare == 0) {
                    compare = stepName.compareTo(o.stepName);
                }
                return compare;
            }

            @Override
            public String toString() {
                return "[stepName='" + stepName + '\'' + ", elapsed=" + (elapsedNanos / NANOS_PER_MILLIS) + "ms]";
            }
        }

    /**
     * 生命周期状态(未启动/运行中/挂起/已停止)
     */
    enum State {
        UNSTART, RUNNING, SUSPENDED, STOPPED
    }

    //region 查询状态
    /** 停表是否已启动 */
    public boolean isStarted() {
        return state == State.RUNNING || state == State.SUSPENDED;
    }

    /** 停表是否在运行中 */
    public boolean isRunning() {
        return state == State.RUNNING;
    }

    /** 停表是否挂起/暂停 */
    public boolean isSuspended() {
        return state == State.SUSPENDED;
    }

    /** 停表是否停止 */
    public boolean isStopped() {
        return state == State.STOPPED;
    }
    //endregion
}
