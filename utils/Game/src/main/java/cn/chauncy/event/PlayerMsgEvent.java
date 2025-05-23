package cn.chauncy.event;

import cn.chauncy.logic.player.Player;
import cn.chauncy.utils.eventbus.GenericEvent;
import com.google.protobuf.Message;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PlayerMsgEvent<T extends Message> implements GenericEvent {

    private final Player player;
    private final T message;

    public PlayerMsgEvent(Player player, T message) {
        this.player = player;
        this.message = message;
    }

    public Player getPlayer() {
        return player;
    }

    public T getMessage() {
        return message;
    }

    @Override
    public @NonNull Class<?> getEventType() {
        return message.getClass();
    }
}
