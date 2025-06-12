package cn.chauncy.logic.task.handler;

import cn.chauncy.logic.task.ConditionType;

public class SpendItemHandler extends ConditionHandler{

    @Override
    public ConditionType getType() {
        return ConditionType.ITEM_SPEND;
    }
}
