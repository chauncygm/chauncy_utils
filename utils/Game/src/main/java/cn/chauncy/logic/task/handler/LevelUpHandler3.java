package cn.chauncy.logic.task.handler;

import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.template.bean.CfgCondition;

public class LevelUpHandler3 extends OuterProgressConditionHandler{


    @Override
    public ConditionType getType() {
        return ConditionType.LEVEL_UP;
    }

    @Override
    protected int getCurrentStateProgress(Player player, CfgCondition condition) {
        return player.getPlayerData().getLevelInfo().getLevel();
    }
}
