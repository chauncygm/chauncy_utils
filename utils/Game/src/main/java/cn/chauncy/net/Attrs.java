package cn.chauncy.net;

import cn.chauncy.logic.player.Player;
import cn.chauncy.utils.RateLimiter;
import io.netty.util.AttributeKey;

import java.util.Map;

public class Attrs {
    public static final String KEY_PLAYER = "player";
    public static final String KEY_PLAYER_ID = "playerUid";
    public static final String KEY_RATE_LIMITER_MAP = "playerRateLimiterMap";

    public static AttributeKey<Player> playerKey = AttributeKey.newInstance(KEY_PLAYER);
    public static AttributeKey<Long> playerIdKey = AttributeKey.newInstance(KEY_PLAYER_ID);
    public static AttributeKey<Map<Integer, RateLimiter>> rateLimiterMapKey = AttributeKey.newInstance(KEY_RATE_LIMITER_MAP);
}
