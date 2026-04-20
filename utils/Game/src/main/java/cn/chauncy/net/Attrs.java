package cn.chauncy.net;

import cn.chauncy.logic.player.Player;
import cn.chauncy.utils.RateLimiter;
import io.netty.util.AttributeKey;

import java.util.Map;

public class Attrs {

    public static AttributeKey<Player> playerKey = AttributeKey.newInstance("player");
    public static AttributeKey<Long> playerIdKey = AttributeKey.newInstance("playerUid");
    public static AttributeKey<Map<Integer, RateLimiter>> rateLimiterMapKey = AttributeKey.newInstance("playerRateLimiterMap");
}
