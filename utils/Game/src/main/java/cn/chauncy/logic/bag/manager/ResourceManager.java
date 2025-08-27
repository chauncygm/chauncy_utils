package cn.chauncy.logic.bag.manager;

import cn.chauncy.logic.player.Player;
import cn.chauncy.message.ResourceChange;
import cn.chauncy.message.SyncResourceChange;
import cn.chauncy.util.MsgUtils;

import java.util.Map;
import java.util.Set;

public class ResourceManager {

    /** 所有资源id集合 */
    private final Set<Integer> resourceIds = Set.of(1, 2, 3, 4, 5, 6);

    /**
     * 添加资源
     * @param player 玩家
     * @param resourceId 资源id
     * @param amount 数量
     */
    public void addResource(Player player, int resourceId, int amount) {
        addResource(player, Map.of(resourceId, amount));
    }

    /**
     * 添加资源
     * @param player 玩家
     * @param resourceMap 资源id与数量
     */
    public void addResource(Player player, Map<Integer, Integer> resourceMap) {
        checkResourceMap(resourceMap);

        Map<Integer, Integer> playerResourceMap = player.getPlayerData().getResourceMap();
        resourceMap.forEach((k, v) -> playerResourceMap.merge(k, v, Integer::sum));

        syncResourceChange(player, resourceMap, true);
    }


    /**
     * 玩家消耗资源
     *
     * @param player      玩家
     * @param resourceId  资源ID
     * @param amount      数量
     * @return 是否消耗成功
     */
    public boolean costResource(Player player, int resourceId, int amount) {
        return costResource(player, Map.of(resourceId, amount));
    }

    /**
     * 玩家消耗资源
     *
     * @param player      玩家
     * @param resourceMap 资源Map
     * @return 是否消耗成功
     */
    public boolean costResource(Player player, Map<Integer, Integer> resourceMap) {
        checkResourceMap(resourceMap);

        Map<Integer, Integer> playerResourceMap = player.getPlayerData().getResourceMap();
        for (Map.Entry<Integer, Integer> entry : resourceMap.entrySet()) {
            if (!isEnough(player, entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        resourceMap.forEach((id, amount) -> {
            costResource(playerResourceMap, id, amount);
        });
        syncResourceChange(player, resourceMap, false);
        return true;
    }

    /**
     * 获取资源数量
     *
     * @param player        玩家
     * @param resourceId    资源id
     */
    public int getResourceAmount(Player player, int resourceId) {
        if (!resourceIds.contains(resourceId)) {
            return 0;
        }

        Map<Integer, Integer> resourceMap = player.getPlayerData().getResourceMap();
        return resourceMap.getOrDefault(resourceId, 0);
    }

    /**
     * 资源是否充足
     *
     * @param player        玩家
     * @param resourceId    资源id
     * @param amount        数量
     */
    public boolean isEnough(Player player, int resourceId, int amount) {
        return getResourceAmount(player,  resourceId) >= amount;
    }

    private void checkResourceMap(Map<Integer, Integer> resourceMap) {
        for (Map.Entry<Integer, Integer> entry : resourceMap.entrySet()) {
            Integer resourceId = entry.getKey();
            Integer amount = entry.getValue();
            if (!resourceIds.contains(resourceId)) {
                throw new IllegalArgumentException("resourceId not exist.");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException("amount must > 0.");
            }
        }
    }

    private void costResource(Map<Integer, Integer> resourceMap, int resourceId, int amount) {
        if (amount <= 0) {
            return;
        }
        int lastAmount = resourceMap.getOrDefault(resourceId, 0);
        int curAmount = Math.max(0, lastAmount - amount);
        if (curAmount == 0) {
            resourceMap.remove(resourceId);
        } else {
            resourceMap.put(resourceId, curAmount);
        }
    }

    private void syncResourceChange(Player player, Map<Integer, Integer> resourceMap, boolean add) {
        Map<Integer, Integer> playerResourceMap = player.getPlayerData().getResourceMap();
        SyncResourceChange.Builder builder = SyncResourceChange.newBuilder();
        for (Map.Entry<Integer, Integer> entry : resourceMap.entrySet()) {
            ResourceChange.Builder changeBuilder = ResourceChange.newBuilder();
            builder.addChanges(changeBuilder.setResourceId(entry.getKey())
                    .setChangeNum(add ? entry.getValue() : -entry.getValue())
                    .setCurrentNum(playerResourceMap.getOrDefault(entry.getKey(), 0)));
        }
        MsgUtils.sendMsg(player, builder);
    }
}
