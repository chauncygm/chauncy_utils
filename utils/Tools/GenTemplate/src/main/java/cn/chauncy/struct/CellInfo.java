package cn.chauncy.struct;

import lombok.Data;

@Data
public class CellInfo {

    /** 单元格对应的字段信息，缓存在这里 */
    private FieldInfo fieldInfo;

    /** 单元格原始值 */
    private String cellValue;

    /** 单元格解析的数据，用于生成json数据 */
    private Object fieldValue;

    public CellInfo() {
    }

    public CellInfo(FieldInfo fieldInfo, String value, Object fieldValue) {
        this.cellValue = value;
        this.fieldInfo = fieldInfo;
        this.fieldValue = fieldValue;
    }
}
