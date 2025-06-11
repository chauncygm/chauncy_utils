package cn.chauncy.dao.entity;

import cn.chauncy.logic.bag.struct.Bag;
import cn.chauncy.logic.bag.struct.BagType;
import cn.chauncy.logic.player.LevelInfo;
import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.logic.task.TaskOriginalData;
import cn.chauncy.logic.task.TaskData;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    /** 玩家名 */
    private String playerName;

    /** 等级数据 */
    private LevelInfo levelInfo;

    /** 资源 */
    private Map<Integer, Integer> resourceMap = new HashMap<>();

    /** 背包数据 */
    private Map<BagType, Bag> bagMap = new HashMap<>();

    /** 玩家身上的任务数据 */
    private Map<Integer, TaskData> taskMap = new HashMap<>();

    /** 已完成的任务Id集合 */
    private Set<Integer> finishedTaskIdSet = new HashSet<>();

    /** 玩家已累积的任务原始数据，针对每个任务条件类型 */
    private Map<ConditionType, TaskOriginalData> playerTaskDataMap = new HashMap<>();

}
