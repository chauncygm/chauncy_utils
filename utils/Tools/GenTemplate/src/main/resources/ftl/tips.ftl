package cn.chauncy.template;

/**
 * ID:${data.id} 字段数:${data.col} 有效数据行数:${data.row} 说明:${data.desc}
 */
public enum CfgTips {
<#list data.cols[0].values as key>
    /**
     * ${data.cols[2].values[key_index][0][0]}
     */
    ${data.cols[1].values[key_index][0][0]}_${key[0][0]}(${key[0][0]}, "${data.cols[2].values[key_index][0][0]}"),
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