package cn.chauncy.services;

import cn.chauncy.utils.time.TimeProvider;
import com.google.common.util.concurrent.AbstractService;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.util.concurrent.DefaultThreadFactory;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventDispatcherService extends AbstractService {

    private static final Logger logger = LoggerFactory.getLogger(EventDispatcherService.class);

    private final TimeProvider timeProvider;
    private final Disruptor<GlobalEvent> disruptor;

    @Inject
    public EventDispatcherService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        disruptor = new Disruptor<>(GlobalEvent.EVENT_FACTORY, 1024, new DefaultThreadFactory("WorldSimulateService"),
                ProducerType.MULTI, new SleepingWaitStrategy());
        disruptor.handleEventsWith(new GlobalEventHandler());
    }


    @Override
    protected void doStart() {
        disruptor.start();
    }

    @Override
    protected void doStop() {
        disruptor.shutdown();
    }

    public void postEvent(GlobalEvent.EventType eventType, long sid, Object... data) {
        disruptor.publishEvent((event, sequence) -> {
            event.eventType = eventType;
            event.sid = sid;
            event.time = timeProvider.getTimeMillis();
            event.data = data;
        });
    }

    private static class GlobalEventHandler implements EventHandler<GlobalEvent> {

        @Override
        public void onEvent(GlobalEvent event, long sequence, boolean endOfBatch) {
            logger.info("Receive global event, sequence: {}, data : {}", sequence, event);
        }
    }


    private static class GlobalEvent {

        private static final EventFactory<GlobalEvent> EVENT_FACTORY = GlobalEvent::new;

        private EventType eventType;
        private long sid;
        private long time;
        private Object[] data;

        public enum EventType {
            SCENE_EVENT,
            RANK_EVENT,
        }
    }
}
