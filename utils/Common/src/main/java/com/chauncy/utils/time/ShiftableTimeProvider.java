package com.chauncy.utils.time;

import java.util.concurrent.TimeUnit;

/**
 * 可偏移时间的时间提供者
 *
 * @author chauncy
 */
public class ShiftableTimeProvider implements TimeProvider {

    /** 原始时间 */
    private final TimeProvider originalTime;

    /** 时间偏移量(毫秒数) */
    private volatile long offset;

    public ShiftableTimeProvider(TimeProvider originalTime) {
        this.originalTime = originalTime;
    }

    public ShiftableTimeProvider(TimeProvider originalTime, long offset) {
        this.originalTime = originalTime;
        this.offset = offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setOffset(int offsetUnit, TimeUnit unit) {
        setOffset(unit.toMillis(offsetUnit));
    }

    public void addOffset(int offsetUnit, TimeUnit unit) {
        setOffset(this.offset + unit.toMillis(offsetUnit));
    }

    @Override
    public long getTimeMillis() {
        return originalTime.getTimeMillis() + offset;
    }
}
