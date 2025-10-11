package cn.chauncy.logic.hero.attr.type;

public enum MergeType {
    /** 叠加 */
    SUM,
    /** 覆盖,总是应用最新值 */
    COVER,
    /** 最大值 */
    MAX,
    /** 最小值 */
    MIN,
    /** 乘数叠加 */
    MUL;

    public static MergeType ofId(int id) {
        return MergeType.values()[id];
    }
}