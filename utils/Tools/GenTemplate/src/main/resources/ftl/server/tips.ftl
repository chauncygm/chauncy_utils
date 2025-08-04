package ${package};

import lombok.Getter;

/**
 * 说明: ${data.sheetComment}表 ID:${data.sheetId} 字段数:${data.sheetContent.fieldInfoMap?size} 有效数据行数:${data.sheetContent.dataInfoList?size}
 * Created on ${.now?string("yyyy-MM-dd HH:mm")}
 */
@Getter
public enum CfgTips {
<#list data.sheetContent.dataInfoList as info>
    ${info.cellValueMap["name"].cellValue}_${info.cellValueMap["id"].cellValue}(${info.cellValueMap["id"].cellValue}, "${info.cellValueMap["tips"].cellValue}"),
</#list>
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