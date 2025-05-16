package eventbus;

import com.chauncy.utils.eventbus.Subscriber;
import org.checkerframework.checker.nullness.qual.NonNull;

public class StringHandler implements Subscriber<String> {

        @Override
        public void onEvent(@NonNull String event) {
            System.out.println("Handle string event: " + event);
        }
    }