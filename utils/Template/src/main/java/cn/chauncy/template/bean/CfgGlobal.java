package cn.chauncy.template.bean;

import java.util.*;
import cn.chauncy.base.Entry;
import cn.chauncy.base.BaseBean;
import cn.chauncy.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 说明: 表ID:10 全局
 * Created on 2025-06-09 16:18
 */
public class CfgGlobal extends BaseBean {

    private static final TypeReference<LinkedHashMap<Integer, CfgGlobal>> MAP_REFERENCE = new TypeReference<>() {};

    /**
     * ID:10 字段数:1 有效数据行数:1 说明:全局
     */
    @JsonIgnore
    public final static String TABLE_NAME = "global";

    /**
     * data
     */
    @JsonIgnore
    private static Map<Integer, CfgGlobal> dataMap = Map.of();

    /**
     * 通过key查询
     *
     * @param key 配置表key
     * @return
     */
    public static CfgGlobal get(int key) {
        return dataMap.get(key);
    }

    /**
     * 遍历
     *
     * @return
     */
    public static Map<Integer, CfgGlobal> all() {
        return dataMap;
    }

    /**
     * 重载配置
     *
     * @param data 配置数据
     * @return
     */
    public static int reload(String data) {
        if (data == null || data.isEmpty()) {
            dataMap.clear();
            return 0;
        }

        dataMap = JsonUtils.readFromJson(data, MAP_REFERENCE);
        dataMap = Collections.unmodifiableMap(dataMap);
        return dataMap.size();
    }

    private CfgGlobal() { }


    /**
     * 最大背包容量 
     */
    private int max_bag_capacity;

    /**
     * 最大背包容量
     */
    public int getMax_bag_capacity() {
        return max_bag_capacity;
    }
}