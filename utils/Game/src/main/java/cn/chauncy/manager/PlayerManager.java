package cn.chauncy.manager;

import cn.chauncy.logic.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;


public class PlayerManager {
    private static final Logger logger = LoggerFactory.getLogger(PlayerManager.class);

    private final ConcurrentHashMap<Long, Player> players = new ConcurrentHashMap<>();

    public Player getPlayer(long playerId) {
        return players.get(playerId);
    }

    public void clear() {
        players.clear();
    }
}
