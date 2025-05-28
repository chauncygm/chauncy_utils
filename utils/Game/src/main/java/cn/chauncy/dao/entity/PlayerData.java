package cn.chauncy.dao.entity;

import cn.chauncy.logic.player.LevelInfo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("player_data")
public class PlayerData extends BaseEntity {

    @EqualsAndHashCode.Include
    @TableId(value = "player_id", type = IdType.INPUT)
    private long playerId;

    private String playerName;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private LevelInfo levelInfo;

}
