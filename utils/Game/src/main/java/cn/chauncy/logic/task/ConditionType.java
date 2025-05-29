package cn.chauncy.logic.task;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum ConditionType implements IEnum<Integer> {

    /** 登录X天 */
    LOGIN_DAYS(1),
    ;


    final int value;

    ConditionType(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
