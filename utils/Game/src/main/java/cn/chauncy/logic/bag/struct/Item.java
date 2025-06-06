package cn.chauncy.logic.bag.struct;

import cn.chauncy.logic.bag.item_type.ItemData;
import cn.chauncy.template.bean.CfgItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Item {

    /** 唯一id */
    private long uid;

    /** 配置id */
    private int configId;

    /** 是否绑定 */
    private boolean bind;

    /** 道具数量 */
    private int count;

    @JsonIgnore
    private ItemType type;

    @JsonIgnore
    private CfgItem cfgItem;

    /** 道具额外数据，与道具类型关联 */
    private ItemData data;

    public void release() {
        uid = 0;
        configId = 0;
        count = 0;
        data = null;
        type = null;
        cfgItem = null;
    }

}
