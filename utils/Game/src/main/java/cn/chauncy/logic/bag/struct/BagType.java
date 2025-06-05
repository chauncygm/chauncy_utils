package cn.chauncy.logic.bag.struct;

import com.baomidou.mybatisplus.annotation.IEnum;

/** 背包类型 */
public enum BagType implements IEnum<Integer> {

    /** 基础道具 */
    BASE(1),

    /** 装备 */
    EQUIP(2),

    /** 材料 */
    MATERIAL(3),

    /** 任务 */
    TASK(4),
    ;

    final int value;

    BagType(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public static BagType valueOf(int value) {
        for (BagType type : BagType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
