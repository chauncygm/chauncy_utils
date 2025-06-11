package cn.chauncy.template.bean;

import java.util.*;
import cn.chauncy.base.Entry;
import cn.chauncy.base.BaseBean;
import cn.chauncy.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 说明: 表ID:21 条件
 * Created on 2025-06-09 16:18
 */
public class CfgCondition extends BaseBean {

    private static final TypeReference<LinkedHashMap<Integer, CfgCondition>> MAP_REFERENCE = new TypeReference<>() {};

    /**
     * ID:21 字段数:4 有效数据行数:6 说明:条件
     */
    @JsonIgnore
    public final static String TABLE_NAME = "condition";

    /**
     * data
     */
    @JsonIgnore
    private static Map<Integer, CfgCondition> dataMap = Map.of();

    /**
     * 通过key查询
     *
     * @param key 配置表key
     * @return
     */
    public static CfgCondition get(int key) {
        return dataMap.get(key);
    }

    /**
     * 遍历
     *
     * @return
     */
    public static Map<Integer, CfgCondition> all() {
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

    private CfgCondition() { }


    /**
     *  条件id
     */
    private int id;
    /**
     *  条件类型
     */
    private int conditionType;
    /**
     * op - target -- 可指定多个参数目标，同一类型长度固定，格式[操作符，目标值] 任务目标参数
     */
    private List<Entry.Int2IntVal> conditionParams;
    /**
     * 默认为1,即达成条件。累积计数型任务目标值>=1 目标值
     */
    private int target;

    /**
     * 
     */
    public int getId() {
        return id;
    }
    /**
     * 
     */
    public int getConditionType() {
        return conditionType;
    }
    /**
     * op - target -- 可指定多个参数目标，同一类型长度固定，格式[操作符，目标值]
     */
    public List<Entry.Int2IntVal> getConditionParams() {
        return conditionParams;
    }
    /**
     * 默认为1,即达成条件。累积计数型任务目标值>=1
     */
    public int getTarget() {
        return target;
    }
}