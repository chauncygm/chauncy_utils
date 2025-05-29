package cn.chauncy.logic.bag.manager;

import cn.chauncy.logic.bag.struct.Item;
import cn.chauncy.logic.bag.struct.ItemType;
import cn.chauncy.utils.guid.GUIDGenerator;
import com.google.inject.Inject;

public class ItemManager {

    private final GUIDGenerator guidGenerator;

    @Inject
    public ItemManager(GUIDGenerator guidGenerator) {
        this.guidGenerator = guidGenerator;
    }


    public Item createItem(int configId, int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("num must > 0.");
        }
        if (configId <= 0) {
            throw new IllegalArgumentException("itemId not exist.");
        }
        Item item = new Item();
        item.setUid(guidGenerator.genGuid());
        item.setConfigId(configId);
        item.setCount(num);
        item.setType(ItemType.NORMAL);
        return item;
    }

}
