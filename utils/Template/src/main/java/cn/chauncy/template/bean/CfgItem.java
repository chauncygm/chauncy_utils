package cn.chauncy.template.bean;

import java.util.*;
import cn.chauncy.base.Entry;
import cn.chauncy.base.BaseBean;
import cn.chauncy.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 说明: 表ID:11 道具表
 * Created on 2025-06-03 18:42
 */
public class CfgItem extends BaseBean {
    /**
     * ID:11 字段数:4 有效数据行数:6 说明:道具表
     */
    @JsonIgnore
    public final static String TABLE_NAME = "item";

    /**
     * data
     */
    @JsonIgnore
    private static Map<Integer, CfgItem> dataMap = null;

    /**
     * 通过key查询
     *
     * @param key 配置表key
     * @return
     */
    public static CfgItem get(int key) {
        return dataMap.get(key);
    }

    /**
     * 遍历
     *
     * @return
     */
    public static Map<Integer, CfgItem> all() {
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

        dataMap = JsonUtils.readFromJson(data, new TypeReference<LinkedHashMap<Integer, CfgItem>>() {});
        dataMap = Collections.unmodifiableMap(dataMap);
        return dataMap.size();
    }

    private CfgItem() { }


    /**
     *  道具id
     */
    private int id;
    /**
     *  道具类型
     */
    private int type;
    /**
     * 0无1白2蓝3紫4橙5红 道具品质
     */
    private int quality;
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
     * 0无1白2蓝3紫4橙5红
     */
    public int getQuality() {
        return quality;
    }
    /**
     * id - num -- 
     */
    public List<Entry.Int2IntVal> getDecompose() {
        return decompose;
    }
}