package cn.chauncy.logic.hero.attr.type;


public enum AttrType {

    /** 生命值 */
    HP(1, 0, 0, 0, 2000000000),
    /** 最大生命值 */
    HP_MAX(2, 0, 0, 0, 2000000000),
    /** 蓝量 */
    MP(3, 0, 0, 0, 2000000000),
    /** 初始蓝量 */
    MP_INIT(4, 0, 0, 0, 2000000000),
    /** 最大蓝量 */
    MP_MAX(5, 0, 0, 0, 2000000000),

    /** 物理攻击 */
    ATK_PHY(6, 0, 0, 0, 2000000000),
    /** 魔法攻击 */
    ATK_MAG(7, 0, 0, 0, 2000000000),
    /** 物理抗性 */
    DEF_PHY(8, 0, 0, 0, 2000000000),
    /** 魔法抗性 */
    DEF_MAG(9, 0, 0, 0, 2000000000),
    /** 物理增伤 */
    DAMAGE_ADD_PHY(10, 0, 0, 0, 2000000000),
    /** 物理减伤 */
    DAMAGE_SUB_PHY(11, 0, 0, 0, 2000000000),
    /** 魔法增伤 */
    DAMAGE_ADD_MAG(12, 0, 0, 0, 2000000000),
    /** 魔法减伤 */
    DAMAGE_SUB_MAG(13, 0, 0, 0, 2000000000),

    /** 初始护盾 */
    SHIELD_INIT(14, 0, 0, 0, 2000000000),
    /** 护盾加成 */
    SHIELD_ADD(15, 0, 0, 0, 2000000000),
    /** 治疗加成 */
    HEAL_ADD(16, 0, 0, 0, 2000000000),

    /** 攻击回蓝 */
    ATK_ADD_BLUE(17, 0, 0, 0, 2000000000),
    /** 受击回蓝 */
    DEF_ADD_BLUE(18, 0, 0, 0, 2000000000),
    /** 固定回蓝 */
    FIX_ADD_BLUE(19, 0, 0, 0, 2000000000),
    /** 吸血比例 */
    HEAL_ADD_PERCENT(20, 0, 0, 0, 2000000000),
    /** 真伤比例 */
    REAL_DAMAGE_PERCENT(21, 0, 0, 0, 2000000000),

    /** 攻击距离 */
    ATK_RANGE(22, 0, 0, 0, 2000000000),
    /** 攻击速度 */
    ATK_SPEED(23, 0, 0, 0, 2000000000),
    /** 移动速度 */
    MOVE_SPEED(24, 0, 0, 0, 2000000000),

    /** 暴击率 */
    CRIT_RATE(25, 0, 0, 0, 2000000000),
    /** 暴击伤害加成 */
    CRIT_DAMAGE(26, 0, 0, 0, 2000000000),


    /** 正常 */
    /** 死亡 */
    /** 冰冻 */
    /** 冰冷 */
    /** 眩晕 */
    /** 击飞 */
    /** 不可选取 */
    /** 不可移动 */
    /** 无敌 */
    /** 免疫物理伤害 */
    /** 免疫魔法伤害 */
    /** 免疫控制 */
    /** 免疫冰冻 */
    /** 免疫致盲 */
    /** 免疫眩晕 */
    /** 免疫击飞 */
    ;

    public static final AttrType[] values = AttrType.values();
    public static final int COUNT = values.length;


    /** 属性ID */
    public final int propId;
    /** 属性类型 */
    public final ShowType propType;
    /** 属性值 */
    public final MergeType mergeType;
    /** 属性下限 */
    public final int propMin;
    /** 属性上限 */
    public final int propMax;

    AttrType(int propId, int propType, int mergeType, int propMin, int propMax) {
        this.propId = propId;
        this.propType = ShowType.ofId(propType);
        this.mergeType = MergeType.ofId(mergeType);
        this.propMin = propMin;
        this.propMax = propMax;
    }

    public static AttrType valueOf(int value) {
        for (AttrType value1 : values) {
            if (value1.propId == value) {
                return value1;
            }
        }
        return null;
    }

}


