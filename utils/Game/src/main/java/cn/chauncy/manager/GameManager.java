package cn.chauncy.manager;

import cn.chauncy.disruptor.GameTickProvider;
import cn.chauncy.gameplay.scene.SceneManager;
import cn.chauncy.utils.time.FrameCachedTimeProvider;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameManager implements GameTickProvider {

    private static final Logger logger = LoggerFactory.getLogger(GameManager.class);

    private static final long MAX_FRAME_USE_TIME = 1000 / 30;
    private static final long MAX_WARN_FRAME_COUNT = 100;

    private int warnFrameCount = 0;
    private long warnStatisticTime = 0;
    private final SceneManager sceneManager;
    private final FrameCachedTimeProvider timeProvider;

    @Inject
    public GameManager(SceneManager sceneManager, FrameCachedTimeProvider timeProvider) {
        this.sceneManager = sceneManager;
        this.timeProvider = timeProvider;
    }

    @Override
    public void tick() {
        timeProvider.update();
        long lastFrameUseTime = timeProvider.getLastFrameUseTime();
        if (lastFrameUseTime > MAX_FRAME_USE_TIME) {
            logger.warn("last frame use time too long: {}ms", lastFrameUseTime);
            warnFrameCount++;
        }
        warnStatisticTime += lastFrameUseTime;
        if (warnStatisticTime > 60 * 1000) {
            if (warnFrameCount > MAX_WARN_FRAME_COUNT) {
                logger.warn("too many warn frame: {}", warnFrameCount);
            }
            logger.info("{}", timeProvider.status());
            warnStatisticTime = 0;
            warnFrameCount = 0;
        }

        sceneManager.tickAllScenes();
    }
}
