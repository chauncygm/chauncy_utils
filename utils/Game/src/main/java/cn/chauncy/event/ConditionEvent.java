package cn.chauncy.event;

import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.task.ConditionType;

import java.util.List;

public interface ConditionEvent {

    /** 主要提升可读性，所有的带条件的变化实践在这里定义 */
    PlayerEvent.ConditionChangeEvent convert();
    
    record GetItemEvent(Player player, int itemId, int num) implements ConditionEvent {
        @Override
        public PlayerEvent.ConditionChangeEvent convert() {
            return new PlayerEvent.ConditionChangeEvent(player, ConditionType.ITEM_GET, List.of(itemId), num);
        }
    }

}
