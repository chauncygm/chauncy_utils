package cn.chauncy.util;

import cn.chauncy.logic.player.Player;
import cn.chauncy.message.SystemTips;
import cn.chauncy.message.TipsType;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MsgUtils {

    private static final Logger logger = LoggerFactory.getLogger(MsgUtils.class);

    public static void sendMsg(Player player, Message.Builder builder) {
        sendMsg(player, builder.build());
    }
    public static void sendMsg(Player player, Message message) {
        ChannelHandlerContext ctx = player.getCtx();
        if (!player.isOnline()) {
            logger.error("send msg failed, player has offline, player: {}", player);
            return;
        }
        sendMsg(ctx, message);
    }
    public static void sendMsg(ChannelHandlerContext ctx, Message.Builder builder) {
        sendMsg(ctx, builder.build());
    }

    public static void sendMsg(ChannelHandlerContext ctx, Message message) {
        if (ctx == null || !ctx.channel().isActive()) {
            logger.error("ctx is inactive, player: {}", ctx);
            return;
        }
        logger.info("send ctx[{}] msg: {}", ctx.channel().id(), message);
        ctx.writeAndFlush(message);
    }

    public static void sendTips(Player player, TipsType type, int code) {
        sendTips(player.getCtx(), type, code, "");
    }

    public static void sendTips(ChannelHandlerContext ctx, TipsType type, int code) {
        sendTips(ctx, type, code, "");
    }

    public static void sendTips(Player player, TipsType type, int code, String content, int... params) {
        sendTips(player.getCtx(), type, code, content,  params);
    }

    public static void sendTips(ChannelHandlerContext ctx, TipsType type, int code, String content, int... params) {
        SystemTips.Builder builder = SystemTips.newBuilder();
        builder.setType(type);
        builder.setCode(code);
        for (int param : params) {
            builder.addParams(param);
        }
        builder.setMessage(content);
        sendMsg(ctx, builder);
    }

}
