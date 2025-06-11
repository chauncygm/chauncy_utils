package cn.chauncy.template.bean;

import java.util.*;
import cn.chauncy.base.Entry;
import cn.chauncy.base.BaseBean;
import cn.chauncy.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 说明: 表ID:20 任务
 * Created on 2025-06-09 16:18
 */
public class CfgTask extends BaseBean {

    private static final TypeReference<LinkedHashMap<Integer, CfgTask>> MAP_REFERENCE = new TypeReference<>() {};

    /**
     * ID:20 字段数:11 有效数据行数:8 说明:任务
     */
    @JsonIgnore
    public final static String TABLE_NAME = "task";

    /**
     * data
     */
    @JsonIgnore
    private static Map<Integer, CfgTask> dataMap = Map.of();

    /**
     * 通过key查询
     *
     * @param key 配置表key
     * @return
     */
    public static CfgTask get(int key) {
        return dataMap.get(key);
    }

    /**
     * 遍历
     *
     * @return
     */
    public static Map<Integer, CfgTask> all() {
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

    private CfgTask() { }


    /**
     *  任务id
     */
    private int id;
    /**
     *  章节
     */
    private int chapter;
    /**
     * 1主线2日常3周常 任务大类型
     */
    private int type;
    /**
     * 0完成全部，x表示需要完成x个目标列表中的条件 组任务类型
     */
    private int groupType;
    /**
     * 0先攒数据，1先接任务 优先度
     */
    private int prior;
    /**
     * 配置条件表id 任务目标列表
     */
    private List<Integer> goalList;
    /**
     * 解锁条件 解锁条件
     */
    private int condition;
    /**
     * itemId - num --  奖励道具
     */
    private List<Entry.Int2IntVal> reward;
    /**
     * 用于限时的任务,不配置表不限时 限时完成时间
     */
    private int limitTime;
    /**
     * 用于限时的任务,不配置表不限时 限时完成时间
     */
    private int rewardLimitTime;
    /**
     *  是否可见
     */
    private int visible;

    /**
     * 
     */
    public int getId() {
        return id;
    }
    /**
     * 
     */
    public int getChapter() {
        return chapter;
    }
    /**
     * 1主线2日常3周常
     */
    public int getType() {
        return type;
    }
    /**
     * 0完成全部，x表示需要完成x个目标列表中的条件
     */
    public int getGroupType() {
        return groupType;
    }
    /**
     * 0先攒数据，1先接任务
     */
    public int getPrior() {
        return prior;
    }
    /**
     * 配置条件表id
     */
    public List<Integer> getGoalList() {
        return goalList;
    }
    /**
     * 解锁条件
     */
    public int getCondition() {
        return condition;
    }
    /**
     * itemId - num -- 
     */
    public List<Entry.Int2IntVal> getReward() {
        return reward;
    }
    /**
     * 用于限时的任务,不配置表不限时
     */
    public int getLimitTime() {
        return limitTime;
    }
    /**
     * 用于限时的任务,不配置表不限时
     */
    public int getRewardLimitTime() {
        return rewardLimitTime;
    }
    /**
     * 是否可见
     */
    public int getVisible() {
        return visible;
    }
}