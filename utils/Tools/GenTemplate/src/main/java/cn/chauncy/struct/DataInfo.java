package cn.chauncy.struct;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import lombok.Data;

import java.util.Map;

@Data
public class DataInfo {

    /** 表格数据索引 */
    private int dataIndex;
    /** 表格行id */
    private int id;
    /** 当前行每列的数据, key:fieldName */
    private Map<String, CellInfo> cellValueMap = new Object2ObjectArrayMap<>();

    public DataInfo(int dataIndex) {
        this.dataIndex = dataIndex;
    }
}
