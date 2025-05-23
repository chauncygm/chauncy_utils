package eventbus;

import cn.chauncy.registry.SubscribeExampleTestRegister;
import cn.chauncy.utils.eventbus.DefaultEventBus;
import cn.chauncy.utils.eventbus.EventBus;
import cn.chauncy.utils.eventbus.Subscribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class SubscribeExampleTest {

    private EventBus eventBus;
    private final AtomicInteger count = new AtomicInteger();

    @Subscribe
    public void onStringEvent(String event) {
        if (count.get() < 10) {
            eventBus.post("This is string: " + count.incrementAndGet());
        }
        System.out.println("Received string event: " + event);
    }

    @Subscribe
    public void onProtoEvent(ProtoEvent<String> event) {
        System.out.println("Received ProtoEvent: " + event);
    }

    @BeforeEach
    public void setup() {
        eventBus = new DefaultEventBus();
    }

    @Test
    public void testSubscribe() {
        eventBus = new DefaultEventBus(false);
        eventBus.register(new StringHandler());
        SubscribeExampleTestRegister.register(eventBus, this);

        eventBus.post("This is a string!");
        eventBus.post(new ProtoEvent<>(1, "This is a event data!"));
    }

    @Test
    public void testSubscribe2() {
        eventBus = new DefaultEventBus(true);
        eventBus.register(new StringHandler());
        SubscribeExampleTestRegister.register(eventBus, this);

        eventBus.post("This is a string!");
        eventBus.post(new ProtoEvent<>(1, "This is a event data!"));
    }

}
