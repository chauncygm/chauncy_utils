package cn.chauncy.event;

import cn.chauncy.logic.player.Player;
import cn.chauncy.utils.eventbus.GenericEvent;
import com.google.protobuf.Message;
import org.checkerframework.checker.nullness.qual.NonNull;

public record PlayerMsgEvent<T extends Message>(Player player, T message) implements GenericEvent {

    @Override
    public @NonNull Class<?> getEventType() {
        return message.getClass();
    }
}
