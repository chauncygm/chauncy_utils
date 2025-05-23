package eventbus;

import cn.chauncy.utils.eventbus.GenericEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ProtoEvent<T> implements GenericEvent {

    private final int protoEnum;
    private final T data;

    public ProtoEvent(int protoEnum, T data) {
        this.data = data;
        this.protoEnum = protoEnum;
    }

    @Override
    public @NonNull Class<?> getEventType() {
        return data.getClass();
    }

    @Override
    public String toString() {
        return "ProtoEvent{" +
                "protoEnum=" + protoEnum +
                ", data=" + data +
                '}';
    }
}