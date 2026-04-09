package cn.chauncy.event;

import cn.chauncy.logic.player.Player;
import cn.chauncy.utils.eventbus.GenericEvent;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.checkerframework.checker.nullness.qual.NonNull;

public record CtxMsgEvent<T extends Message>(ChannelHandlerContext context, Player player, T message) implements GenericEvent {

    @Override
    public @NonNull Class<?> getEventType() {
        return message.getClass();
    }
}
