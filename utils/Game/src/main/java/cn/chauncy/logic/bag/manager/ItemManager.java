package cn.chauncy.logic.bag.manager;

import cn.chauncy.base.Entry;
import cn.chauncy.logic.bag.struct.Item;
import cn.chauncy.logic.bag.struct.ItemType;
import cn.chauncy.template.bean.CfgItem;
import cn.chauncy.utils.guid.GUIDGenerator;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {

    private final GUIDGenerator guidGenerator;

    @Inject
    public ItemManager(GUIDGenerator guidGenerator) {
        this.guidGenerator = guidGenerator;
    }

    public List<Item> createItems(List<Entry.Int2IntVal> itemId2NumList) {
        List<Item> items = new ArrayList<>(itemId2NumList.size());
        for (Entry.Int2IntVal int2IntVal : itemId2NumList) {
            items.add(createItem(int2IntVal));
        }
        return items;
    }

    public Item createItem(Entry.Int2IntVal int2IntVal) {
        if (int2IntVal.k() <= 0 || int2IntVal.v() <= 0) {
            throw new IllegalArgumentException("itemId or num must > 0.");
        }
        return createItem(int2IntVal.k(), int2IntVal.v());
    }

    public Item createItem(int configId, int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("num must > 0.");
        }

        CfgItem cfgItem = CfgItem.get(configId);
        if (cfgItem == null) {
            throw new IllegalArgumentException("itemId not exist.");
        }
        if (cfgItem.getType() == ItemType.COIN.value) {
            throw new IllegalArgumentException("coin can not create item.");
        }

        Item item = new Item();
        item.setUid(guidGenerator.genGuid());
        item.setConfigId(configId);
        item.setCount(num);
        item.setType(ItemType.valueOf(cfgItem.getType()));
        item.setCfgItem(cfgItem);
        return item;
    }

    public CfgItem getCfgItem(Item item) {
        if (item.getConfigId() <= 0) {
            throw new IllegalArgumentException("item configId not exist.");
        }

        if (item.getCfgItem() != null && item.getCfgItem().getId() == item.getConfigId()) {
            return item.getCfgItem();
        }
        return CfgItem.get(item.getConfigId());
    }

}
