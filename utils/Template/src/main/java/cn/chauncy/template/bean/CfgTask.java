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
 * 说明: 任务表 ID:20 字段数:12 有效数据行数:8
 * Created on 2025-08-04 17:19
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public class CfgTask {

    private CfgTask() {}

    /** 任务id */
    private int id;
    /** 章节 */
    private int chapter;
    /** 任务大类型 */
    private int type;
    /** 任务描述 */
    private String desc;
    /** 组任务类型 */
    private int groupType;
    /** 任务数据类型 */
    private int sourceType;
    /** 任务目标列表 */
    @JsonDeserialize(using = ImmutableListDeserializer.class)
    private List<Integer> goalList;
    /** 解锁条件 */
    private int condition;
    /** 奖励道具 */
    @JsonDeserialize(using = ImmutableListDeserializer.class)
    private List<Entry.Int2IntVal> reward;
    /** 限时完成时间 */
    private int limitTime;
    /** 限时完成时间 */
    private int rewardLimitTime;
    /** 是否可见 */
    private int visible;


    public static final String TABLE_NAME = "task";
    private static Map<Integer, CfgTask> dataMap = Map.of();
    private static final TypeReference<LinkedHashMap<Integer, CfgTask>> MAP_REFERENCE = new TypeReference<>() {};

    public static CfgTask get(int key) {
        return dataMap.get(key);
    }

    public static Map<Integer, CfgTask> all() {
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