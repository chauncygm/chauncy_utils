package cn.chauncy.logic.function;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum FunctionId implements IEnum<Integer> {

    /** 未知功能 */
    NONE(0),

    /** 背包系统 */
    BAG(1),

    /** 任务系统 */
    TASK(2),

    /** 社交系统 */
    SOCIAL(3),
    ;

    final int value;

    FunctionId(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return 0;
    }
}
