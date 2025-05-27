package cn.chauncy.dao.entity;

import cn.chauncy.logic.player.LevelExp;
import cn.chauncy.utils.mapper.AutoMapper;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@AutoMapper(baseMapper = BaseMapper.class)
@TableName("player_data")
public class PlayerData {
    @TableId(value = "player_id", type = IdType.INPUT)
    private long id;
    private String playerName;
    private LevelExp levelExp;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

//    public LevelExp getLevelExp() {
//        return levelExp;
//    }
//
//    public void setLevelExp(LevelExp levelExp) {
//        this.levelExp = levelExp;
//    }
}
