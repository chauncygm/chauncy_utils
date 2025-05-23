package cn.chauncy.utils.time;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 缓存时间
 * 仅提供毫秒级精度
 */
public class CachedTime implements TimeProvider {

    private volatile long currentTime = System.currentTimeMillis();

    public CachedTime() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> currentTime = System.currentTimeMillis(),
                1, 1, TimeUnit.MILLISECONDS); // 1ms精度
    }

    @Override
    public long getTimeMillis() {
        return currentTime;
    }
}
