package cn.chauncy.logic.login;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.event.CtxMsgEvent;
import cn.chauncy.event.PlayerEvent;
import cn.chauncy.event.PlayerMsgEvent;
import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.player.PlayerManager;
import cn.chauncy.message.*;
import cn.chauncy.net.Attrs;
import cn.chauncy.util.MsgUtils;
import cn.chauncy.utils.eventbus.Subscribe;
import cn.chauncy.utils.time.TimeProvider;
import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.chauncy.logic.common.TipCode.LOGIN_REPEATED;

public class LoginManager {

    private static final Logger logger = LoggerFactory.getLogger(LoginManager.class);

    private final TimeProvider timeProvider;
    private final PlayerManager playerManager;
    private final GlobalEventBus globalEventBus;

    @Inject
    public LoginManager(PlayerManager playerManager, TimeProvider timeProvider, GlobalEventBus globalEventBus) {
        this.playerManager = playerManager;
        this.timeProvider = timeProvider;
        this.globalEventBus = globalEventBus;
    }

    @Subscribe
    public void onPlayerLogin(CtxMsgEvent<ReqLogin> msgEvent) {
        ChannelHandlerContext context = msgEvent.context();
        ReqLogin reqLogin = msgEvent.message();
        long uid = reqLogin.getUid();
        logger.info("onPlayerLogin, uid: {}", uid);
        playerLogin(context, uid);
    }

    public void playerLogin(ChannelHandlerContext ctx, long uid) {
        Player player = getOrCreatePlayer(uid);
        if (player.isOnline()) {
            ChannelHandlerContext onlineCtx = player.getCtx();
            if (onlineCtx != null && onlineCtx.channel().isActive()) {
                MsgUtils.sendTips(player, TipsType.WINDOW, LOGIN_REPEATED, "login repeated kick out offline");
                onlineCtx.close();
                player.setCtx(null);
            }
        }
        player.setCtx(ctx);
        ctx.channel().attr(Attrs.playerKey).set(player);
        ctx.channel().attr(Attrs.playerIdKey).set(player.getPlayerId());
        globalEventBus.post(new PlayerEvent.PlayerLoginEvent(player));
        playerManager.online(player);
        if (player.isNewPlayer()) {
            globalEventBus.post(new PlayerEvent.PlayerCreateRoleEvent(player));
        }
        player.setNewPlayer(false);
        globalEventBus.post(new PlayerEvent.PlayerOnlineEvent(player));

        syncLoginData(player);
    }

    private Player getOrCreatePlayer(long uid) {
        long playerId = playerManager.getPlayerId(uid);
        Player player = null;
        if (playerId > 0) {
            player = playerManager.getPlayerAutoLoad(playerId);
        }
        if (player == null) {
            player = playerManager.createPlayer(uid);
        }
        return player;
    }

    public void syncLoginData(Player player) {
        cn.chauncy.dao.entity.PlayerData playerData = player.getPlayerData();
        SyncLoginData.Builder builder = SyncLoginData.newBuilder();
        builder.setUid(playerData.getUid());
        PlayerData.Builder playerDataBuilder = PlayerData.newBuilder();
        playerDataBuilder.setPlayerId(playerData.getPlayerId());
        playerDataBuilder.setName("chauncy");
        builder.setPlayerData(playerDataBuilder.build());
        MsgUtils.sendMsg(player, builder);
    }

    @Subscribe
    public void onReqHeartbeat(PlayerMsgEvent<ReqHeartbeat> msgEvent) {
        ChannelHandlerContext context = msgEvent.player().getCtx();
        ResHeartbeat.Builder builder = ResHeartbeat.newBuilder();
        builder.setTime(timeProvider.getTimeMillis());
        MsgUtils.sendMsg(context, builder);
    }

    @Subscribe
    public void onReqLogout(PlayerMsgEvent<ReqLogout> msgEvent) {
        Player player = msgEvent.player();
        logger.info("onReqLogout: {}", player.info());
        playerManager.offline(player);
    }
}
