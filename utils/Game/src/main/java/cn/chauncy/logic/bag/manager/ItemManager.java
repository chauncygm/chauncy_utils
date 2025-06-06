package cn.chauncy.logic.bag.manager;

import cn.chauncy.base.Entry;
import cn.chauncy.logic.bag.struct.BagType;
import cn.chauncy.logic.bag.struct.Item;
import cn.chauncy.logic.bag.struct.ItemType;
import cn.chauncy.template.bean.CfgItem;
import cn.chauncy.utils.guid.GUIDGenerator;
import com.google.inject.Inject;
import org.checkerframework.checker.nullness.qual.NonNull;

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

    /**
     * 从道具中分离出指定数量的道具
     * @param item  指定道具
     * @param num   拆分的数量
     * @return      拆分出的新道具
     */
    public Item splitFromItem(@NonNull Item item, int num) {
        if (num > item.getCount()) {
            throw new IllegalArgumentException("item count not enough.");
        }
        if (num == item.getCount()) {
            return item;
        }

        item.setCount(item.getCount() - num);

        Item newItem = new Item();
        newItem.setUid(guidGenerator.genGuid());
        newItem.setConfigId(item.getConfigId());
        newItem.setCount(num);
        newItem.setType(item.getType());
        newItem.setCfgItem(item.getCfgItem());
        newItem.setData(item.getData());
        return newItem;
    }

    /**
     * 指定道具是否可合并到目标道具
     *
     * @param fromItem  源道具
     * @param toItem    目标道具
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isMergeable(@NonNull Item fromItem, @NonNull Item toItem) {
        if (fromItem.getConfigId() != toItem.getConfigId()) {
            return false;
        }
        if (fromItem.getCount() <= 0 || toItem.getCount() <= 0) {
            return false;
        }
        if (fromItem.isBind() != toItem.isBind()) {
            return false;
        }
        return toItem.getCount() < getCfgItem(toItem).getMaxStack();
    }

    /**
     * 合并两个道具
     *
     * @param fromItem  源道具
     * @param toItem    目标道具
     */
    public void mergeItem(@NonNull Item fromItem, @NonNull Item toItem) {
        if (fromItem.getConfigId() != toItem.getConfigId()) {
            throw new IllegalArgumentException("itemId not equal.");
        }
        if (fromItem.getCount() <= 0 || toItem.getCount() <= 0) {
            throw new IllegalArgumentException("item count <= 0");
        }
        if (fromItem.isBind() != toItem.isBind()) {
            throw new IllegalArgumentException("item bind not equal.");
        }
        if (fromItem.getCount() + toItem.getCount() > getCfgItem(toItem).getMaxStack()) {
            throw new IllegalArgumentException("item stack overflow.");
        }

        toItem.setCount(toItem.getCount() + fromItem.getCount());
        fromItem.release();
    }

    /**
     * 获取道具的配置信息
     */
    public @NonNull CfgItem getCfgItem(@NonNull Item item) {
        if (item.getConfigId() <= 0) {
            throw new IllegalArgumentException("item configId not exist.");
        }

        if (item.getCfgItem() != null && item.getCfgItem().getId() == item.getConfigId()) {
            return item.getCfgItem();
        }
        CfgItem cfgItem = CfgItem.get(item.getConfigId());
        if (cfgItem == null) {
            throw new IllegalArgumentException("itemId not exist.");
        }
        item.setCfgItem(cfgItem);
        return cfgItem;
    }

    /**
     * 获取道具的背包类型
     * @param item  指定道具
     * @return      背包类型
     */
    public @NonNull BagType getBagType(@NonNull Item item) {
        CfgItem cfgItem = getCfgItem(item);
        BagType bagType = BagType.valueOf(cfgItem.getBagType());
        if (bagType == null) {
            throw new IllegalArgumentException("item bagType not exist.");
        }
        return bagType;
    }

    /**
     * @param item 此id的道具
     * 是否可堆叠
     */
    public boolean isStackable(@NonNull Item item) {
        return getCfgItem(item).getMaxStack() > 1;
    }

    /**
     * 获取指定道具可堆叠的数量
     * @param item  指定道具
     * @return      指定道具还可以堆叠的数量
     */
    public int remainStackableNum(@NonNull Item item) {
        if (item.getCount() <= 0) {
            throw new IllegalArgumentException("item count <= 0");
        }
        int maxStack = getCfgItem(item).getMaxStack();
        return Math.max(0, maxStack - item.getCount());
    }

}
