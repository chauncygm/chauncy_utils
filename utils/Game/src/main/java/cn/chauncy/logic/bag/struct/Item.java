package cn.chauncy.logic.bag.struct;

import cn.chauncy.logic.bag.item_type.ItemData;
import lombok.Data;

@Data
public class Item {

    private long uid;

    private int configId;

    private int count;

    private ItemType type;

    /** 道具额外数据，与道具类型关联 */
    private ItemData data;

}
