package cn.chauncy.dao.mapper;

import cn.chauncy.dao.dto.PlayerIdUidDTO;
import cn.chauncy.dao.entity.PlayerData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface PlayerDataMapper extends BaseMapper<PlayerData> {

    @Select("SELECT uid, player_id FROM player_data")
    List<PlayerIdUidDTO> selectIdUidMap();

}
