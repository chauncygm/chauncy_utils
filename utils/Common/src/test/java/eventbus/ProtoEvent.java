package eventbus;

import com.chauncy.utils.eventbus.GenericEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ProtoEvent<T> implements GenericEvent {

        private final int msgId;
        private final T data;

        public ProtoEvent(int msgId, T data) {
            this.data = data;
            this.msgId = msgId;
        }

        @Override
        public @NonNull Class<?> getEventType() {
            return data.getClass();
        }

        @Override
        public String toString() {
            return "ProtoEvent{" +
                    "msgId=" + msgId +
                    ", data=" + data +
                    '}';
        }
    }