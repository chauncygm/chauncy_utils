package cn.chauncy.logic.player;

import cn.chauncy.dao.entity.PlayerData;
import cn.chauncy.logic.player.component.GoalComponent;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class Player {

    private long playerId;
    private ChannelHandlerContext ctx;
    private volatile boolean online;

    private PlayerData playerData;

    @Getter
    private final GoalComponent goalComponent = new GoalComponent();

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public ChannelHandlerContext getCtx() {
        return Objects.requireNonNull(ctx);
    }

    public void setCtx(@NonNull ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public PlayerData getPlayerData() {
        return Objects.requireNonNull(playerData);
    }

    public void setPlayerData(@NonNull PlayerData playerData) {
        this.playerData = playerData;
    }
}
