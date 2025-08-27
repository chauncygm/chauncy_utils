package cn.chauncy.logic.gm;

import cn.chauncy.dao.entity.PlayerData;
import cn.chauncy.dao.mapper.PlayerDataMapper;
import cn.chauncy.event.PlayerMsgEvent;
import cn.chauncy.logic.Managers;
import cn.chauncy.logic.player.LevelInfo;
import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.player.PlayerManager;
import cn.chauncy.message.*;
import cn.chauncy.util.MsgUtils;
import cn.chauncy.utils.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.protobuf.ProtocolStringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class GMManager {

    private static final Logger logger = LoggerFactory.getLogger(GMManager.class);

    private final Map<String, Method> gmMethodMap = new HashMap<>();

    private final Managers managers;

    @Inject
    public GMManager(Managers managers) {
        this.managers = managers;
        registerAllGMMethod();
    }

    public void registerAllGMMethod() {
        for (Method method : this.getClass().getMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (method.getReturnType() != String.class || method.getParameterCount() != 2) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes[0] != Player.class || parameterTypes[1] != String[].class) {
                continue;
            }
            gmMethodMap.put(method.getName().toLowerCase(), method);
        }
    }

    @Subscribe
    public void onReqGM(PlayerMsgEvent<ReqGm> msgEvent) {
        Player player = msgEvent.player();
        ReqGm reqGm = msgEvent.message();
        String cmd = reqGm.getCmd().toLowerCase();
        if (!gmMethodMap.containsKey(cmd)) {
            sendGmResult(player, CStatus.PARAM_ERROR);
            return;
        }
        Method method = gmMethodMap.get(cmd);
        try {
            ProtocolStringList paramsList = reqGm.getParamsList();
            String[] params = paramsList.toArray(new String[0]);
            Object result = method.invoke(this, player, params);
            sendGmResult(player, CStatus.SUCCESS, result.toString());
        } catch (Exception e) {
            logger.error("GMManager.onReqGM error", e);
            sendGmResult(player, CStatus.SERVER_ERROR);
        }
    }

    private void sendGmResult(Player player, CStatus status) {
        sendGmResult(player, status, "");
    }
    @SuppressWarnings("SameParameterValue")
    private void sendGmResult(Player player, CStatus status, String message) {
        sendGmResult(player, status, message, "");
    }

    @SuppressWarnings("SameParameterValue")
    private void sendGmResult(Player player, CStatus status, String message, String data) {
        MsgUtils.sendMsg(player, ResGm.newBuilder().setStatus(status).setMessage(message).setData(data));
    }

    //region gm命令
    /**
     * 设置等级/经验
     * addResource id num
     */
    public String setLvExp(Player player, String[] params){
        int lv = Integer.parseInt(params[0]);
        int exp = Integer.parseInt(params[1]);
        LevelInfo levelInfo = player.getPlayerData().getLevelInfo();
        levelInfo.setLevel(Math.max(lv, 0));
        levelInfo.setExp(Math.max(exp, 0));
        managers.playerManager.updatePlayer(player);

        MsgUtils.sendMsg(player, SyncLevelExpChange.newBuilder().setLevel(levelInfo.getLevel()).setExp(levelInfo.getExp()));
        return "";
    }

    /**
     * 添加资源
     * addResource id num
     */
    public String addResource(Player player, String[] params){
        int resourceId = Integer.parseInt(params[0]);
        int resourceNum = Integer.parseInt(params[1]);
        if (resourceNum > 0) {
            managers.resourceManager.addResource(player, resourceId, resourceNum);
        } else {
            managers.resourceManager.costResource(player, resourceId, -resourceNum);
        }
        return "";
    }
    //endregion

}
