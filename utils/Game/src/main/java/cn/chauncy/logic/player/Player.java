package cn.chauncy.logic.player;

import io.netty.channel.ChannelHandlerContext;

public class Player {

    private ChannelHandlerContext ctx;
    private volatile boolean online;
}
