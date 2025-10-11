package cn.chauncy.logic.hero.attr.type;

public enum ShowType {
    /** 普通属性 */
    NORMAL,
    /** 百分比 */
    PERCENT;
    public static ShowType ofId(int id) {
        return ShowType.values()[id];
    }
}