package cn.chauncy.template;

import lombok.Getter;

/**
 * 说明: 提示表 ID:99 字段数:3 有效数据行数:8
 * Created on 2025-08-04 17:19
 */
@Getter
public enum CfgTips {
    SUCCESS_0(0, "操作完成"),
    SERVER_ERROR_1(1, "服务器异常"),
    ILLEGAL_OP_2(2, "非法操作"),
    CLIENT_PARAM_ERROR_3(3, "客户端参数错误"),
    BAG_NOT_OPEN_4(4, "背包未开启"),
    BAG_FULL_5(5, "背包容量不足"),
    ITEM_NOT_ENOUGH_6(6, "背包道具不足"),
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

    public static CfgTips valueOf(int id) {
        for (CfgTips tips : values()) {
            if (tips.id == id)
                return tips;
        }
        return null;
    }
}