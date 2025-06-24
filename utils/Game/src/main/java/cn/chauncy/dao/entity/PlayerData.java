package cn.chauncy.dao.entity;

import cn.chauncy.logic.bag.struct.Bag;
import cn.chauncy.logic.bag.struct.BagType;
import cn.chauncy.logic.player.LevelInfo;
import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.logic.task.TaskData;
import cn.chauncy.logic.task.TaskOriginalData;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("player_data")
public class PlayerData extends BaseEntity {

    @EqualsAndHashCode.Include
    @TableId(value = "player_id", type = IdType.INPUT)
    private long playerId;

    /** 玩家uid */
    private long uid;

    /** 玩家名 */
    private String playerName;

    /** 等级数据 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private LevelInfo levelInfo = new LevelInfo();

    /** 资源 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<Integer, Integer> resourceMap = new HashMap<>();

    /** 背包数据 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<BagType, Bag> bagMap = new HashMap<>();

    /** 玩家身上的任务数据 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<Integer, TaskData> taskMap = new HashMap<>();

    /** 已完成的任务Id集合 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<Integer> finishedTaskIdSet = new HashSet<>();

    /** 玩家已累积的任务原始数据，针对每个任务条件类型 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<ConditionType, TaskOriginalData> playerTaskDataMap = new HashMap<>();

    /** 上次登录时间 */
    private long lastLoginTime;

    /** 上次跨天时间 */
    private long lastCrossDayTime;

    /** 上次离线时间 */
    private long lastOfflineTime;

}
