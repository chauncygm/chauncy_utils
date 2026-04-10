package cn.chauncy.utils.time;

public class FrameCachedTimeProvider implements TimeProvider {

    private long startTime;
    private long currentTime;
    private long frameCount;
    private long lastFrameTime;

    @Override
    public long getTimeMillis() {
        return currentTime;
    }

    public void update() {
        long time = System.currentTimeMillis();
        lastFrameTime = currentTime != 0 ? currentTime : time - 30;
        currentTime = time;
        if (startTime == 0) {
            startTime = time;
        }
        frameCount += 1;
    }

    public long getFrameCount() {
        return frameCount;
    }

    public long getLastFrameUseTime() {
        return currentTime - lastFrameTime;
    }

    public void reset() {
        frameCount = 0;
        startTime = 0;
        currentTime = 0;
        lastFrameTime = 0;
    }

    public String status() {
        return "{" +
                "startTime=" + startTime +
                ", currentTime=" + currentTime +
                ", frameCount=" + frameCount +
                ", FPS=" + (frameCount * 1000 / (currentTime - startTime)) +
                '}';
    }
}
