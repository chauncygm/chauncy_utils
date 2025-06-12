package cn.chauncy.logic.task.handler;

import cn.chauncy.logic.task.ConditionType;

public class LoginDaysHandler extends ConditionHandler{

    @Override
    public ConditionType getType() {
        return ConditionType.LOGIN_DAYS;
    }
}
