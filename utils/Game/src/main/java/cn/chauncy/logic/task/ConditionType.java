package cn.chauncy.logic.task;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum ConditionType implements IEnum<Integer> {

    /** 登录X天 */
    LOGIN_DAYS(1),

    /** 在线X分钟 */
    ONLINE_MINUTES(2),

    /** 升级到X级 */
    LEVEL_UP(3),

    /** 获得道具 */
    ITEM_GET(4),

    /** 消耗道具 */
    ITEM_CONSUME(5),

    /** 完成指定ID的任务 */
    FINISH_TASK(6),
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
