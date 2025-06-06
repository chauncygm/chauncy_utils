package cn.chauncy.logic.bag.manager;

import cn.chauncy.base.Entry;
import cn.chauncy.dao.entity.PlayerData;
import cn.chauncy.logic.bag.struct.Bag;
import cn.chauncy.logic.bag.struct.BagItem;
import cn.chauncy.logic.bag.struct.BagType;
import cn.chauncy.logic.bag.struct.Item;
import cn.chauncy.logic.player.Player;
import cn.chauncy.template.CfgTips;
import cn.chauncy.template.bean.CfgItem;
import com.google.inject.Inject;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class BagManager {

    /** 默认的背包排序规则 */
    private static final Comparator<BagItem> BAG_ITEM_COMPARATOR = Comparator.<BagItem>comparingInt((v) -> v.getItem().getConfigId())
            .thenComparingLong(v -> v.getItem().getUid());

    private final ItemManager itemManager;
    private final ResourceManager resourceManager;

    @Inject
    public BagManager(ItemManager itemManager, ResourceManager resourceManager) {
        this.itemManager = itemManager;
        this.resourceManager = resourceManager;
    }

    /** 开放新背包 */
    public void openBag(Player player, BagType bagType) {
        PlayerData playerData = player.getPlayerData();
        Map<Integer, Bag> bagMap = playerData.getBagMap();
        if (!bagMap.containsKey(bagType.getValue())) {
            bagMap.put(bagType.getValue(), createBag(bagType));
        }
    }

    /**
     * 创建背包
     *
     * @param bagType   背包类型
     * @return          背包
     */
    private Bag createBag(BagType bagType) {
        if (bagType == BagType.RESOURCE) {
            throw new UnsupportedOperationException("资源类型无需创建背包");
        }
        Bag bag = new Bag();
        bag.setBagType(bagType);
        bag.setBagSize(40);
        return bag;
    }

    /**
     * 获取背包
     *
     * @param player       玩家
     * @param bagType      背包类型
     * @return             背包
     */
    public Bag getBag(Player player, BagType bagType) {
        if (bagType == BagType.RESOURCE) {
            return null;
        }
        Map<Integer, Bag> bagMap = player.getPlayerData().getBagMap();
        return bagMap.get(bagType.getValue());
    }

    /**
     * 获取空闲格子数
     *
     * @param bag   背包
     * @return      空闲格子数
     */
    public int getIdleGridSize(Bag bag) {
        return Math.max(bag.getBagSize() - bag.getBagItems().size(), 0);
    }

    /**
     * 检查整理背包道具
     */
    public void checkBagItem(Player player, boolean neaten) {
        Map<Integer, Bag> bagMap = player.getPlayerData().getBagMap();
        for (Bag bag : bagMap.values()) {
            checkBagItem(bag, neaten);
        }
    }

    //region 添加道具

    /**
     * 添加道具
     *
     * @param player    玩家
     * @param items     道具列表
     * @return          错误码
     */
    public CfgTips rewardItems(Player player, List<Item> items) {
        if (items.size() == 1) {
            Item item = items.get(0);
            BagType bagType = itemManager.getBagType(item);
            CfgTips cfgTips = testRewardItems(player, bagType, List.of(item));
            if (!cfgTips.compare(CfgTips.SUCCESS_0)) {
                return cfgTips;
            }
            rewardBagItem(player, bagType, List.of(item));
            return CfgTips.SUCCESS_0;
        }

        Map<BagType, List<Item>> itemTypeListMap = rewardGroupOf(items);
        for (Map.Entry<BagType, List<Item>> entry : itemTypeListMap.entrySet()) {
            CfgTips cfgTips = testRewardItems(player, entry.getKey(), entry.getValue());
            if (!cfgTips.compare(CfgTips.SUCCESS_0)) {
                return cfgTips;
            }
        }
        for (Map.Entry<BagType, List<Item>> entry : itemTypeListMap.entrySet()) {
            rewardBagItem(player, entry.getKey(), entry.getValue());
        }
        return CfgTips.SUCCESS_0;
    }

    /**
     * 测试添加道具
     *
     * @param player       玩家
     * @param bagType      背包类型
     * @param items        道具列表
     * @return             提示码
     */
    private CfgTips testRewardItems(Player player, BagType bagType, List<Item> items) {
        if (bagType == BagType.RESOURCE) {
            return CfgTips.SUCCESS_0;
        }

        Bag bag = getBag(player, bagType);
        if (bag == null) {
            return CfgTips.BAG_NOT_OPEN_4;
        }

        int idleGridSize = getIdleGridSize(bag);
        for (Item item : items) {
            idleGridSize = testAddSingleItem(bag, item, idleGridSize);
            if (idleGridSize < 0) {
                return CfgTips.BAG_FULL_5;
            }
        }
        return CfgTips.SUCCESS_0;
    }

    /**
     * 测试添加单个道具
     *
     * @param bag           背包
     * @param item          道具
     * @param idleBagSize   当前背包剩余格子数
     * @return              剩余背包格子数，-1表示背包格子已溢出
     */
    private int testAddSingleItem(Bag bag, Item item, int idleBagSize) {
        int remainItemCount = item.getCount();
        List<BagItem> bagItems = findBagItemByConfigId(bag, item.getConfigId());
        for (BagItem bagItem : bagItems) {
            if (!itemManager.isMergeable(item, bagItem.getItem())) {
                continue;
            }
            int num = itemManager.remainStackableNum(bagItem.getItem());
            int splitNum = Math.min(num, remainItemCount);
            if (splitNum > 0) {
                remainItemCount -= splitNum;
            }
            if (remainItemCount <= 0) {
                return idleBagSize;
            }
        }

        int stackableNum = itemManager.getCfgItem(item).getMaxStack();
        int needGridCount = remainItemCount / stackableNum + (remainItemCount % stackableNum > 0 ? 1 : 0);
        if (needGridCount > idleBagSize) {
            return -1;
        }
        return idleBagSize - needGridCount;
    }

    /**
     * 添加道具
     *
     * @param player   玩家
     * @param bagType  背包类型
     * @param items    道具列表
     */
    private void rewardBagItem(Player player, BagType bagType, @NonNull List<Item> items) {
        if (bagType == BagType.RESOURCE) {
            Map<Integer, Integer> resourceMap = items.size() == 1 ?
                    Map.of(items.get(0).getConfigId(), items.get(0).getCount()) :
                    items.stream().collect(Collectors.toMap(Item::getConfigId, Item::getCount, Integer::sum));
            resourceManager.addResource(player, resourceMap);
            return;
        }

        Bag bag = getBag(player, bagType);
        if (bag == null) {
            throw new IllegalStateException("背包不存在: " + bagType);
        }

        BitSet positionBitSet = bag.getPositionBitSet();
        if (positionBitSet.cardinality() != bag.getBagItems().size()) {
            throw new IllegalStateException("背包位位空闲数与已占用数不一致");
        }

        for (Item item : items) {
            rewardSingleItemMaximize(bag, item);
        }
    }

    /**
     * 最大化添加单个道具
     * @param bag   背包
     * @param item  道具
     */
    public void rewardSingleItemMaximize(@NonNull Bag bag, @NonNull Item item) {
        int remainItemCount = item.getCount();

        // 如果可堆叠先从已有的道具中合并
        if (itemManager.isStackable(item)) {
            List<BagItem> bagItems = findBagItemByConfigId(bag, item.getConfigId());
            for (BagItem bagItem : bagItems) {
                if (!itemManager.isMergeable(item, bagItem.getItem())) {
                    continue;
                }
                int num = itemManager.remainStackableNum(bagItem.getItem());
                int splitNum = Math.min(num, remainItemCount);
                if (splitNum > 0) {
                    Item splitItem = itemManager.splitFromItem(item, splitNum);
                    itemManager.mergeItem(splitItem, bagItem.getItem());
                    remainItemCount -= splitNum;
                }
                if (remainItemCount <= 0) {
                    break;
                }
            }
        }

        if (remainItemCount <= 0) {
            return;
        }

        // 找空闲格子创建新的道具
        int idleGridSize = getIdleGridSize(bag);
        int stackableNum = itemManager.getCfgItem(item).getMaxStack();
        int needGridCount = remainItemCount / stackableNum + (remainItemCount % stackableNum > 0 ? 1 : 0);
        int gridCount = Math.min(idleGridSize, needGridCount);

        BitSet positionBitSet = bag.getPositionBitSet();
        for (int i = 0; i < gridCount; i++) {
            Item splitItem = itemManager.splitFromItem(item, stackableNum);
            int insertPos = positionBitSet.nextClearBit(0);
            BagItem bagItem = new BagItem(insertPos, splitItem);
            bag.getBagItems().put(bagItem.getItem().getUid(), bagItem);
            positionBitSet.set(insertPos);
        }
    }

    private List<BagItem> findBagItemByConfigId(Bag bag, int configId) {
        Collection<BagItem> bagItems = bag.getBagItems().values();
        return bagItems.stream()
                .filter(bagItem -> bagItem.getItem().getConfigId() == configId)
                .collect(Collectors.toList());
    }
    //endregion

    //region 消耗道具

    /**
     * 消耗道具
     *
     * @param player    玩家
     * @param items     道具列表
     * @return          错误码
     */
    public CfgTips spendItems(Player player, List<Entry.Int2IntVal> items) {
        if (items == null || items.isEmpty()) {
            return CfgTips.SUCCESS_0;
        }
        Map<BagType, List<Entry.Int2IntVal>> bagTypeListMap = spendGroupOf(items);
        for (Map.Entry<BagType, List<Entry.Int2IntVal>> entry : bagTypeListMap.entrySet()) {
            CfgTips cfgTips = testSpendBagItem(player, entry.getKey(), entry.getValue());
            if (cfgTips != CfgTips.SUCCESS_0) {
                return cfgTips;
            }
        }

        for (Map.Entry<BagType, List<Entry.Int2IntVal>> entry : bagTypeListMap.entrySet()) {
            spendBagItem(player, entry.getKey(), entry.getValue());
        }
        return CfgTips.SUCCESS_0;
    }


    /**
     * 检测背包物品是否满足消耗
     *
     * @param player    玩家
     * @param bagType   背包类型
     * @param items     道具列表
     * @return          错误码
     */
    private CfgTips testSpendBagItem(Player player, BagType bagType, Collection<Entry.Int2IntVal> items) {
        if (bagType == BagType.RESOURCE) {
            for (Entry.Int2IntVal item : items) {
                if (!resourceManager.isEnough(player, item.k(), item.v())) {
                    return CfgTips.RESOURCE_NOT_ENOUGH_7;
                }
            }
        }

        Bag bag = getBag(player, bagType);
        if (bag == null) {
            return CfgTips.BAG_NOT_OPEN_4;
        }

        for (Entry.Int2IntVal item : items) {
            CfgTips cfgTips = testSpendSingleItem(bag, item);
            if (cfgTips != CfgTips.SUCCESS_0) {
                return cfgTips;
            }
        }
        return CfgTips.SUCCESS_0;
    }

    /**
     * 测试是否可以消耗
     *
     * @param bag   背包
     * @param item  道具列表
     * @return      错误码
     */
    private CfgTips testSpendSingleItem(Bag bag, Entry.Int2IntVal item) {
        List<BagItem> bagItems = findBagItemByConfigId(bag, item.k());
        int itemCount = 0;
        for (BagItem bagItem : bagItems) {
            itemCount += bagItem.getItem().getCount();
            if (itemCount >= item.v()) {
                return CfgTips.SUCCESS_0;
            }
        }
        return CfgTips.ITEM_NOT_ENOUGH_6;
    }

    /**
     * 扣除背包物品
     *
     * @param player    玩家
     * @param bagType   背包类型
     * @param items     道具列表
     */
    private void spendBagItem(Player player, BagType bagType, @NonNull List<Entry.Int2IntVal> items) {
        if (bagType == BagType.RESOURCE) {
            Map<Integer, Integer> resourceMap = items.stream()
                    .collect(Collectors.toMap(Entry.Int2IntVal::k, Entry.Int2IntVal::v, Integer::sum));
            resourceManager.costResource(player, resourceMap);
            return;
        }
        Bag bag = getBag(player, bagType);
        if (bag == null) {
            throw new IllegalStateException("背包不存在: " + bagType);
        }
        for (Entry.Int2IntVal item : items) {
            spendSingleItem(bag, item);
        }
    }

    /**
     * 消耗单个物品
     *
     * @param bag   背包
     * @param item  道具
     */
    private void spendSingleItem(Bag bag, Entry.Int2IntVal item) {
        List<BagItem> bagItems = findBagItemByConfigId(bag, item.k());
        int remainNum = item.v();
        BitSet positionBitSet = bag.getPositionBitSet();
        for (BagItem bagItem : bagItems) {
            int itemCount = bagItem.getItem().getCount();
            if (itemCount > remainNum) {
                bagItem.getItem().setCount(itemCount - remainNum);
                remainNum = 0;
                break;
            } else {
                bag.getBagItems().remove(bagItem.getItem().getUid());
                positionBitSet.clear(bagItem.getPosition());
                remainNum -= itemCount;
            }
        }

        if (remainNum > 0) {
            throw new IllegalStateException("背包道具不足");
        }
    }

    //endregion

    //region 私有方法
    public Map<BagType, List<Item>> rewardGroupOf(Collection<Item> items) {
        Map<BagType, List<Item>> map = new IdentityHashMap<>();
        for (Item item : items) {
            BagType bagType = itemManager.getBagType(item);
            List<Item> list = map.computeIfAbsent(bagType, k -> new ArrayList<>());
            list.add(item);
        }
        return map;
    }

    public Map<BagType, List<Entry.Int2IntVal>> spendGroupOf(Collection<Entry.Int2IntVal> items) {
        Map<BagType, List<Entry.Int2IntVal>> map = new IdentityHashMap<>();
        for (Entry.Int2IntVal val : items) {
            CfgItem cfgItem = CfgItem.get(val.k());
            if (cfgItem == null) {
                throw new IllegalStateException("道具不存在: " + val.k());
            }
            BagType bagType = BagType.valueOf(cfgItem.getBagType());
            List<Entry.Int2IntVal> list = map.computeIfAbsent(bagType, k -> new ArrayList<>());
            list.add(val);
        }
        return map;
    }

    /** 整理背包 */
    private void checkBagItem(Bag bag, boolean neaten) {
        if (neaten) {
            List<BagItem> list = bag.getBagItems().values().stream()
                    .sorted(BAG_ITEM_COMPARATOR)
                    .toList();
            for (int i = 0; i < list.size(); i++) {
                BagItem bagItem = list.get(i);
                bagItem.setPosition(i + 1);
            }
        }
        BitSet positionBitSet = new BitSet(bag.getBagItems().size());
        for (BagItem bagItem : bag.getBagItems().values()) {
            positionBitSet.set(bagItem.getPosition());
        }
        bag.setPositionBitSet(positionBitSet);
    }
    //endregion
}
