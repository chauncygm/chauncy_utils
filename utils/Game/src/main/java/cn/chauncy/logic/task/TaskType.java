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

    /** 隐藏 */
    HIDE(6),
    ;

    final int value;

    TaskType(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public static TaskType valueOf(int value) {
        for (TaskType taskType : values()) {
            if (taskType.value == value) {
                return taskType;
            }
        }
        return null;
    }
}
