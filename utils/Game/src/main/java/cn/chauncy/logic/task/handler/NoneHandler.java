package cn.chauncy.logic.task.handler;

import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.template.bean.CfgCondition;

public class NoneHandler extends OuterProgressConditionHandler {

    @Override
    protected int getCurrentStateProgress(Player player, CfgCondition condition) {
        return 0;
    }

    @Override
    public ConditionType getType() {
        return ConditionType.NONE;
    }
}
