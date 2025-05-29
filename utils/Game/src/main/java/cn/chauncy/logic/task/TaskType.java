package cn.chauncy.logic.task;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum TaskType implements IEnum<Integer> {
    /** 主线 */
    MAIN(1),

    /** 日常 */
    DAILY(2),

    /** 周常 */
    WEEKLY(3),

    /** 支线 */
    BRANCH(4),

    /** 成就 */
    ACHIEVEMENT(5),
    ;

    final int value;

    TaskType(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return 0;
    }
}
