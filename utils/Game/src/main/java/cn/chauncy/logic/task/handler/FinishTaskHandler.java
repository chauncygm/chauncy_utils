package cn.chauncy.logic.task.handler;

import cn.chauncy.base.Entry;
import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.template.bean.CfgCondition;

public class FinishTaskHandler extends OuterProgressConditionHandler{

    @Override
    protected int getCurrentStateProgress(Player player, CfgCondition condition) {
        Entry.Int2IntVal int2IntVal = condition.getConditionParams().get(0);
        int taskId = int2IntVal.v();
        return player.getPlayerData().getFinishedTaskIdSet().contains(taskId) ? 1 : 0;
    }

    @Override
    public ConditionType getType() {
        return ConditionType.FINISH_TASK;
    }
}
