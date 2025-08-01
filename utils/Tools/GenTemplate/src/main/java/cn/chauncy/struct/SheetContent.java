package cn.chauncy.struct;


import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SheetContent {

    /** 所有字段信息 */
    private Map<String, FieldInfo> fieldInfoMap = new Object2ObjectArrayMap<>();

    /** 所有数据，参数表只有一条数据 */
    private List<DataInfo> dataInfoList = new ObjectArrayList<>();
}
