package cn.chauncy.services;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.disruptor.GameEventExceptionHandler;
import cn.chauncy.disruptor.GameTickEventProcessor;
import cn.chauncy.disruptor.GameTickProvider;
import cn.chauncy.disruptor.TimeoutSleepingWaitStrategy;
import cn.chauncy.utils.time.TimeProvider;
import com.google.common.util.concurrent.AbstractService;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.util.concurrent.DefaultThreadFactory;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 支持游戏主循环 Tick 的事件分发服务
 * <br/>
 * 特性：
 * 1. 每帧处理事件不超过上限（默认1024个）
 * 2. 保证最低 30 FPS 的游戏主循环 tick
 * 3. 在10ms内无事件时自动执行 tick
 *
 * @author chauncy
 */
public class GameTickEventDispatcherService extends AbstractService {

    private static final Logger logger = LoggerFactory.getLogger(GameTickEventDispatcherService.class);

    /**
     * 每次处理的最大事件数
     */
    private static final int MAX_EVENTS_PER_TICK = 1024;

    private final TimeProvider timeProvider;
    private final TimeoutSleepingWaitStrategy waitStrategy;
    private final Disruptor<GlobalEvent> disruptor;
    private final GlobalEventBus globalEventBus;
    private final GameTickProvider gameTickProvider;

    @Inject
    public GameTickEventDispatcherService(
        TimeProvider timeProvider, 
        GlobalEventBus globalEventBus,
        GameTickProvider gameTickProvider
    ) {
        this.timeProvider = timeProvider;
        this.globalEventBus = globalEventBus;
        this.gameTickProvider = gameTickProvider;
        
        // 创建 WaitStrategy
        this.waitStrategy = new TimeoutSleepingWaitStrategy();
        
        // 创建 Disruptor
        this.disruptor = new Disruptor<>(
            GlobalEvent.EVENT_FACTORY, 
            4 * 1024,
            new DefaultThreadFactory("WorldSimulateService"),
            ProducerType.MULTI, 
            waitStrategy
        );

        RingBuffer<GlobalEvent> ringBuffer = disruptor.getRingBuffer();
        GameTickEventProcessor<GlobalEvent> eventProcessor = new GameTickEventProcessor<>(
                ringBuffer,
                ringBuffer.newBarrier(),
                new GlobalEventHandler(globalEventBus),
                gameTickProvider, MAX_EVENTS_PER_TICK
        );

        disruptor.handleEventsWith(eventProcessor);
        disruptor.setDefaultExceptionHandler(new GameEventExceptionHandler<>());
    }

    @Override
    protected void doStart() {
        logger.info("Starting GameTickEventDispatcherService...");
        disruptor.start();
        logger.info("GameTickEventDispatcherService started successfully.");
    }

    @Override
    protected void doStop() {
        logger.info("Stopping GameTickEventDispatcherService...");
        disruptor.shutdown();
        logger.info("GameTickEventDispatcherService stopped.");
    }

    /**
     * 发布全局事件
     *
     * @param eventType 事件类型
     * @param sid       场景 ID
     * @param data      事件数据
     */
    public void postEvent(GlobalEvent.EventType eventType, long sid, Object... data) {
        disruptor.publishEvent((event, sequence) -> {
            event.eventType = eventType;
            event.sid = sid;
            event.time = timeProvider.getTimeMillis();
            event.data = data;
        });
    }


    /**
     * 全局事件处理器
     */
    private static class GlobalEventHandler implements EventHandler<GlobalEvent> {

        private final GlobalEventBus globalEventBus;

        public GlobalEventHandler(GlobalEventBus globalEventBus){
            this.globalEventBus = globalEventBus;
        }

        @Override
        public void onEvent(GlobalEvent event, long sequence, boolean endOfBatch) {
            logger.debug("Receive global event, sequence: {}, type: {}, sid: {}, createTime: {}, data: {}",
                    sequence, event.eventType, event.sid, event.time, event.data);

            // 根据事件类型处理
            switch (event.eventType) {
                case PLAYER_MSG_EVENT:
                    // 处理玩家消息事件(从网络层转发过来的)
                    if (event.data != null && event.data.length > 0) {
                        Object msgEvent = event.data[0];
                        globalEventBus.post(msgEvent);
                    }
                    break;
                case HTTP_EVENT:
                    // 处理 HTTP 事件
                default:
                    logger.error("Unknown global event type: {}", event.eventType);
                    break;
            }
        }
    }

    /**
     * 全局事件类
     */
    public static class GlobalEvent {

        public static final EventFactory<GlobalEvent> EVENT_FACTORY = GlobalEvent::new;

        private EventType eventType;
        private long sid;
        private long time;
        private Object[] data;

        public enum EventType {
            PLAYER_MSG_EVENT,  // 玩家协议消息事件(从网络层转发)
            HTTP_EVENT,
        }
    }
}
