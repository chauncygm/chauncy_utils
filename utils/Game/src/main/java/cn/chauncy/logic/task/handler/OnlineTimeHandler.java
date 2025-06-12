package cn.chauncy.logic.task.handler;

import cn.chauncy.logic.task.ConditionType;

public class OnlineTimeHandler extends ConditionHandler{

    @Override
    public ConditionType getType() {
        return ConditionType.ONLINE_TIME;
    }
}
