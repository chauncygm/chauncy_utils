package cn.chauncy.logic.bag.struct;

import cn.chauncy.logic.bag.item_type.ItemData;
import cn.chauncy.template.bean.CfgItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Item {

    private long uid;

    private int configId;

    private int count;

    @JsonIgnore
    private ItemType type;

    @JsonIgnore
    private CfgItem cfgItem;

    /** 道具额外数据，与道具类型关联 */
    private ItemData data;

}
