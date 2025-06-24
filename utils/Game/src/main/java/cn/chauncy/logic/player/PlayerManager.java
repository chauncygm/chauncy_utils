package cn.chauncy.logic.player;

import cn.chauncy.dao.dto.PlayerIdUidDTO;
import cn.chauncy.dao.entity.PlayerData;
import cn.chauncy.dao.mapper.PlayerDataMapper;
import cn.chauncy.event.SystemEvent;
import cn.chauncy.utils.eventbus.Subscribe;
import cn.chauncy.utils.guid.GUIDGenerator;
import cn.chauncy.utils.time.TimeProvider;
import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private static final Logger logger = LoggerFactory.getLogger(PlayerManager.class);

    private static final long PLAYER_CACHE_TIME = 1 * 60 * 1000L;
    private final ConcurrentHashMap<Long, Player> playerIdMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> playerUid2IdMap = new ConcurrentHashMap<>();
    private final Set<Long> offlinePlayerSet = ConcurrentHashMap.newKeySet();

    private final GUIDGenerator guidGenerator;
    private final PlayerDataMapper playerDataMapper;
    private final TimeProvider timeProvider;

    @Inject
    public PlayerManager(GUIDGenerator guidGenerator, PlayerDataMapper playerDataMapper, TimeProvider timeProvider) {
        this.guidGenerator = guidGenerator;
        this.playerDataMapper = playerDataMapper;
        this.timeProvider = timeProvider;
    }

    public void loadAllPlayerInfo() {
        List<PlayerIdUidDTO> playerIdUidDTOS = playerDataMapper.selectIdUidMap();
        for (PlayerIdUidDTO dto : playerIdUidDTOS) {
            playerUid2IdMap.put(dto.getUid(), dto.getPlayerId());
        }
        logger.info("load player uid size: {}", playerUid2IdMap.size());
    }

    public Long getPlayerId(long uid) {
        return playerUid2IdMap.getOrDefault(uid, -1L);
    }

    public Player getPlayer(long playerId) {
        return playerIdMap.get(playerId);
    }

    public Player getPlayerAutoLoad(long playerId) {
        Player player = getPlayer(playerId);
        if (player == null) {
            PlayerData playerData = playerDataMapper.selectById(playerId);
            if (playerData != null) {
                player = new Player();
                player.setPlayerData(playerData);
                player.setPlayerId(playerData.getPlayerId());
            }
        }
        return player;
    }

    public Player createPlayer(long uid) {
        Player player = new Player();
        PlayerData playerData = new PlayerData();
        playerData.setUid(uid);
        playerData.setPlayerId(guidGenerator.genGuid());
        playerData.setPlayerName("玩家" + uid);
        player.setPlayerData(playerData);
        player.setNewPlayer(true);
        playerDataMapper.insert(playerData);
        return player;
    }

    @Subscribe
    public void tickOfflinePlayer(SystemEvent.MinuteTickEvent event) {
        for (Long playerId : offlinePlayerSet) {
            Player player = playerIdMap.get(playerId);
            if (player == null || player.isOnline()) {
                continue;
            }
            if (player.getPlayerData().getLastOfflineTime() + PLAYER_CACHE_TIME <= timeProvider.getTimeMillis()) {
                playerIdMap.remove(playerId);
                logger.info("player cache remove: {}", player.info());
            }
        }
    }

    public void online(Player player) {
        player.setOnline(true);
        playerIdMap.put(player.getPlayerId(), player);
        offlinePlayerSet.remove(player.getPlayerId());
        PlayerData playerData = player.getPlayerData();
        playerUid2IdMap.put(playerData.getUid(), playerData.getPlayerId());
    }

    public void offline(Player player) {
        player.setOnline(false);
        player.getPlayerData().setLastOfflineTime(timeProvider.getTimeMillis());
        ChannelHandlerContext ctx = player.getCtx();
        if (ctx != null && ctx.channel().isActive()) {
            ctx.close();
        }
        offlinePlayerSet.add(player.getPlayerId());
        logger.info("player offline: {}", player.info());
    }
}
