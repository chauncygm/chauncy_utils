package cn.chauncy.logic.task.handler;

import cn.chauncy.base.Entry;
import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.logic.task.TaskOriginalData;
import cn.chauncy.template.bean.CfgCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 累积型条件类型处理器
 * 新类型处理器如无需特殊处理可直接使用此类
 */
public class CountingProgressConditionHandler extends OuterProgressConditionHandler {

    private final ConditionType type;

    public CountingProgressConditionHandler(ConditionType type) {
        this.type = type;
    }

    @Override
    public ConditionType getType() {
        return type;
    }

    @Override
    protected int getCurrentStateProgress(Player player, CfgCondition condition) {
        return countingProgress(player, condition);
    }

    /**
     * 累积型条件类型，从历史记录数据中匹配获取
     */
    private int countingProgress(Player player, CfgCondition cfgCondition) {
        TaskOriginalData taskOriginalData = player.getPlayerData().getPlayerTaskDataMap().get(type);
        if (taskOriginalData == null) {
            return 0;
        }
        List<Entry.Int2IntVal> conditionParams = cfgCondition.getConditionParams();
        if (conditionParams.isEmpty() || conditionParams.size() > 4) {
            logger.error("cfgCondition config error, id: {}", cfgCondition.getId());
            return 0;
        }

        List<Map.Entry<TaskOriginalData.ConditionData, Integer>> matchList = new ArrayList<>();
        for (Map.Entry<TaskOriginalData.ConditionData, Integer> entry : taskOriginalData.getDataList().entrySet()) {
            TaskOriginalData.ConditionData conditionData = entry.getKey();
            boolean match = isMatch(conditionParams, (i) -> conditionData.getParams()[i]);
            if (match) {
                matchList.add(entry);
            }
        }
        return merge(matchList);
    }

    /**
     * 统计匹配数据的进度，默认累加
     * 允许重写此方法，如求平均值，最小值，最大值，统计种类数量等
     */
    protected int merge(List<Map.Entry<TaskOriginalData.ConditionData, Integer>> matchList) {
        return matchList.stream()
                .map(Map.Entry::getValue)
                .reduce(Integer::sum)
                .orElse(0);
    }
}
