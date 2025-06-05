package cn.chauncy.logic.bag.struct;

/** 道具类型 */
public enum ItemType {

    /** 货币 */
    COIN(0),

    /** 普通道具 */
    NORMAL(1),

    /** 材料 */
    MATERIAL(2),

    /** 装备 */
    EQUIP(3),

    /** 任务道具 */
    TASK(4),

    ;

    public final int value;

    ItemType(int value) {
        this.value = value;
    }

    public static ItemType valueOf(int value) {
        for (ItemType itemType : values()) {
            if (itemType.value == value) {
                return itemType;
            }
        }
        return null;
    }
}
