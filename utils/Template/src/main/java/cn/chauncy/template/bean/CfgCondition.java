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
 * 说明: 条件表 ID:21 字段数:5 有效数据行数:6
 * Created on 2025-08-05 14:56
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public class CfgCondition {

    private CfgCondition() {}

    /** 条件id */
    private int id;
    /** 条件类型 */
    private int conditionType;
    /** 任务数据类型 */
    private int sourceType;
    /** 任务目标参数 */
    @JsonDeserialize(using = ImmutableListDeserializer.class)
    private List<Entry.Int2IntVal> conditionParams;
    /** 目标值 */
    private int target;


    public static final String TABLE_NAME = "condition";
    private static Map<Integer, CfgCondition> dataMap = Map.of();
    private static final TypeReference<LinkedHashMap<Integer, CfgCondition>> MAP_REFERENCE = new TypeReference<>() {};

    public static CfgCondition get(int key) {
        return dataMap.get(key);
    }

    public static Map<Integer, CfgCondition> all() {
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