package cn.chauncy.event;

import cn.chauncy.logic.player.Player;

public class PlayerEvent {

    record MainLevelChangeEvent(Player player, int level) {}

    record FunctionOpenEvent(Player player, int functionId) {}

    record FunctionCloseEvent(Player player, int functionId) {}

}
