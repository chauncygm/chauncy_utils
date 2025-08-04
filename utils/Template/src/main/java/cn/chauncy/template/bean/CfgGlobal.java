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
 * 说明: 全局表 ID:10 字段数:1 有效数据行数:1
 * Created on 2025-08-04 17:19
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public class CfgGlobal {

    private CfgGlobal() {}

    /** 最大背包容量 */
    private int maxCapacity;


    public static final String TABLE_NAME = "global";
    private static Map<Integer, CfgGlobal> dataMap = Map.of();
    private static final TypeReference<LinkedHashMap<Integer, CfgGlobal>> MAP_REFERENCE = new TypeReference<>() {};

    public static CfgGlobal get(int key) {
        return dataMap.get(key);
    }

    public static Map<Integer, CfgGlobal> all() {
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