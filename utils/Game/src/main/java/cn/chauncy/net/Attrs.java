package cn.chauncy.net;

import cn.chauncy.logic.player.Player;
import io.netty.util.AttributeKey;

public class Attrs {
    public static final String KEY_USER_SESSION = "player_session";

    public static AttributeKey<Player> PLAYER_SESSION = AttributeKey.newInstance(KEY_USER_SESSION);
}
