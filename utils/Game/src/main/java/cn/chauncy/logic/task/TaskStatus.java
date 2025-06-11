package cn.chauncy.logic.task;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum TaskStatus implements IEnum<Integer> {

    /** 接取，进行中状态 */
    FETCHED(0),

    /** 未领奖，已完成状态 */
    FINISHED(1),

    /** 已提交，完结状态 */
    SUBMITTED(2)
    ;

    final int value;

    TaskStatus(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

}
