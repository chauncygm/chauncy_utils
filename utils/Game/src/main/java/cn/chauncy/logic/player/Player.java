package cn.chauncy.logic.player;

import cn.chauncy.dao.entity.PlayerData;
import io.netty.channel.ChannelHandlerContext;

public class Player {

    private long playerId;
    private ChannelHandlerContext ctx;
    private volatile boolean online;

    private PlayerData playerData;

    public long getPlayerId() {
        return playerId;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public boolean isOnline() {
        return online;
    }
}
