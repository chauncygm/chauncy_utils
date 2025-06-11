package cn.chauncy.template.bean;

import java.util.*;
import cn.chauncy.base.Entry;
import cn.chauncy.base.BaseBean;
import cn.chauncy.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 说明: 表ID:11 背包
 * Created on 2025-06-09 16:18
 */
public class CfgBag extends BaseBean {

    private static final TypeReference<LinkedHashMap<Integer, CfgBag>> MAP_REFERENCE = new TypeReference<>() {};

    /**
     * ID:11 字段数:7 有效数据行数:18 说明:背包
     */
    @JsonIgnore
    public final static String TABLE_NAME = "bag";

    /**
     * data
     */
    @JsonIgnore
    private static Map<Integer, CfgBag> dataMap = Map.of();

    /**
     * 通过key查询
     *
     * @param key 配置表key
     * @return
     */
    public static CfgBag get(int key) {
        return dataMap.get(key);
    }

    /**
     * 遍历
     *
     * @return
     */
    public static Map<Integer, CfgBag> all() {
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

    private CfgBag() { }


    /**
     *  道具id
     */
    private int id;
    /**
     *  道具类型
     */
    private int type;
    /**
     *  所属背包
     */
    private int bagType;
    /**
     * 0无1白2蓝3紫4橙5红 道具品质
     */
    private int quality;
    /**
     *  是否绑定
     */
    private int bind;
    /**
     *  最大堆叠数量
     */
    private int maxStack;
    /**
     * id - num --  分解获得
     */
    private List<Entry.Int2IntVal> decompose;

    /**
     * 
     */
    public int getId() {
        return id;
    }
    /**
     * 
     */
    public int getType() {
        return type;
    }
    /**
     * 
     */
    public int getBagType() {
        return bagType;
    }
    /**
     * 0无1白2蓝3紫4橙5红
     */
    public int getQuality() {
        return quality;
    }
    /**
     * 
     */
    public int getBind() {
        return bind;
    }
    /**
     * 
     */
    public int getMaxStack() {
        return maxStack;
    }
    /**
     * id - num -- 
     */
    public List<Entry.Int2IntVal> getDecompose() {
        return decompose;
    }
}