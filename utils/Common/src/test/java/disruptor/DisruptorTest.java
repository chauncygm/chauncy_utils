package disruptor;

import cn.chauncy.utils.Utils;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
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
        Utils.setJULLogger();
        disruptor = new Disruptor<>(factory, 128,
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


        try {
            Thread.sleep(10000);
            disruptor.shutdown(20, TimeUnit.SECONDS);

            disruptor.publishEvent((event, sequence) -> {
                event.setValue(111);
                logger.info("publish event value: {}, sequence: {}", event.getValue(), sequence);
            });

        } catch (TimeoutException e) {
            System.out.println("timeout");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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

    class MyEventhandler implements EventHandler<Event> {
        @Override
        public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
            logger.info("event value: {}, sequence: {}, endOfBatch: {}", event.getValue(), sequence, endOfBatch);
        }
    }

}
