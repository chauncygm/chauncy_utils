package cn.chauncy.logic.bag.struct;

import com.baomidou.mybatisplus.annotation.IEnum;

/** 背包类型 */
public enum BagType implements IEnum<Integer> {

    /** 基础道具 */
    BASE(1),

    /** 资源 */
    RESOURCE(2),

    /** 装备 */
    EQUIP(3),

    /** 材料 */
    MATERIAL(4),

    /** 任务 */
    TASK(5),
    ;

    final int value;

    BagType(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
