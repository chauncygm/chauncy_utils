package cn.chauncy.template;

/**
 * ID:99 字段数:3 有效数据行数:3 说明:提示表
 */
public enum CfgTips {
    /**
     * 服务器异常
     */
    SERVER_ERROR1(1, "服务器异常"),
    /**
     * 背包容量不足
     */
    BAG_FULL2(2, "背包容量不足"),
    /**
     * 资源不足
     */
    RESOURCE_NOT_ENOUGH3(3, "资源不足"),
    ;

    /*
     * id(只能往后增加)
     */
    private final int id;
    /*
     * 文字内容
     */
    private final String content;

    CfgTips(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public boolean compare(CfgTips tips) {
        return this.id == tips.getId();
    }

    public static CfgTips convert(int id) {
        for (CfgTips tips : values()) {
            if (tips.getId() == id)
                return tips;
        }
        return null;
    }
}