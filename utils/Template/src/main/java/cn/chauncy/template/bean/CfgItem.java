package cn.chauncy.template.bean;

import java.util.*;
import cn.chauncy.base.Entry;
import cn.chauncy.utils.json.JsonUtils;
import cn.chauncy.utils.json.ImmutableListDeserializer;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;

/**
 * 说明: 道具表 ID:12 字段数:7 有效数据行数:18
 * Created on 2025-08-05 14:56
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public class CfgItem {

    private CfgItem() {}

    /** 道具id */
    private int id;
    /** 道具类型 */
    private int type;
    /** 所属背包 */
    private int bagType;
    /** 道具品质 */
    private int quality;
    /** 是否绑定 */
    private int bind;
    /** 最大堆叠数量 */
    private int maxStack;
    /** 分解获得 */
    @JsonDeserialize(using = ImmutableListDeserializer.class)
    private List<Entry.Int2IntVal> decompose;


    public static final String TABLE_NAME = "item";
    private static Map<Integer, CfgItem> dataMap = Map.of();
    private static final TypeReference<LinkedHashMap<Integer, CfgItem>> MAP_REFERENCE = new TypeReference<>() {};

    public static CfgItem get(int key) {
        return dataMap.get(key);
    }

    public static Map<Integer, CfgItem> all() {
        return dataMap;
    }

    public static int reload(String data) {
        if (data == null || data.isEmpty()) {
            dataMap.clear();
            return 0;
        }
        dataMap = JsonUtils.readFromJson(data, MAP_REFERENCE);
        dataMap = Collections.unmodifiableMap(dataMap);
        return dataMap.size();
    }
}