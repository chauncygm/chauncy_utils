package cn.chauncy.event;

import cn.chauncy.logic.player.Player;

public class SceneEvent {

    record PlayerJoinSceneEvent(Player player, int sceneId) {}

}
