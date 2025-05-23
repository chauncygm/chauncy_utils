package disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class DisruptorTest {
    private static final Logger logger = LoggerFactory.getLogger(DisruptorTest.class);

    private Disruptor<Event> disruptor;
    private final EventFactory<Event> factory = new EventFactory<>() {

        private final AtomicLong sequence = new AtomicLong(0);
        @Override
        public Event newInstance() {
            return new Event(sequence.getAndIncrement());
        }
    };

    @BeforeEach
    void beforeAll() {
        disruptor = new Disruptor<>(factory, 2048,
                new DefaultThreadFactory("disruptor-test"), ProducerType.MULTI, new SleepingWaitStrategy());
        disruptor.handleEventsWith(new MyEventhandler());
        disruptor.start();
    }

    @Test
    public void test() {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    final int num = j;
                    disruptor.publishEvent((event, sequence) -> {
                        event.setValue(num);
                        logger.info("publish event value: {}, sequence: {}", event.getValue(), sequence);
                    });

                }
            }).start();
        }

    }

    static class Event {
        private long value;

        public Event(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    static class MyEventhandler implements EventHandler<Event> {
        @Override
        public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
            logger.info("event value: {}, sequence: {}, endOfBatch: {}", event.getValue(), sequence, endOfBatch);
        }
    }

}
