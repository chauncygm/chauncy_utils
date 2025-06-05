package cn.chauncy.dao.entity;

import cn.chauncy.logic.bag.struct.Bag;
import cn.chauncy.logic.player.LevelInfo;
import cn.chauncy.logic.task.Task;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("player_data")
public class PlayerData extends BaseEntity {

    @EqualsAndHashCode.Include
    @TableId(value = "player_id", type = IdType.INPUT)
    private long playerId;

    private String playerName;

    private LevelInfo levelInfo;

    private Map<Integer, Integer> resourceMap;

    private Map<Integer, Bag> bagMap;

    private Map<Integer, Task> taskMap;

}
