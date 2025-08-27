package cn.chauncy.logic.function;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum FunctionId implements IEnum<Integer> {

    /** 未知功能 */
    NONE(0),

    /** 背包系统 */
    BAG(1),

    /** 任务系统 */
    TASK(2),

    /** 商店系统 */
    SHOP(3),

    /** 商城充值系统 */
    CHARGE(4),

    /** 签到 */
    SIGN(5),

    /** 社交系统 */
    SOCIAL(6),
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
