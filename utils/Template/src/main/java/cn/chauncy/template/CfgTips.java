package cn.chauncy.template;

/**
 * ID:99 字段数:3 有效数据行数:8 说明:提示
 */
public enum CfgTips {
    /**
     * 操作完成
     */
    SUCCESS_0(0, "操作完成"),
    /**
     * 服务器异常
     */
    SERVER_ERROR_1(1, "服务器异常"),
    /**
     * 非法操作
     */
    ILLEGAL_OP_2(2, "非法操作"),
    /**
     * 客户端参数错误
     */
    CLIENT_PARAM_ERROR_3(3, "客户端参数错误"),
    /**
     * 背包未开启
     */
    BAG_NOT_OPEN_4(4, "背包未开启"),
    /**
     * 背包容量不足
     */
    BAG_FULL_5(5, "背包容量不足"),
    /**
     * 背包道具不足
     */
    ITEM_NOT_ENOUGH_6(6, "背包道具不足"),
    /**
     * 资源不足
     */
    RESOURCE_NOT_ENOUGH_7(7, "资源不足"),
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