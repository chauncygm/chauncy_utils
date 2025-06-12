package cn.chauncy.logic.task.handler;

import cn.chauncy.base.Entry;
import cn.chauncy.event.PlayerEvent;
import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.logic.task.TaskOriginalData;
import cn.chauncy.template.bean.CfgCondition;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

@Getter
public abstract class ConditionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(ConditionHandler.class);

    private final boolean outerProgress;

    public ConditionHandler() {
        this.outerProgress = false;
    }

    public ConditionHandler(boolean outerProgress) {
        this.outerProgress = outerProgress;
    }

    /**
     * 对应的条件类型
     */
    public abstract ConditionType getType();

    /**
     * 获取指定条件的进度
     */
    public int getCurrentProgress(Player player, CfgCondition cfgCondition) {
        if (cfgCondition.getSourceType() != 0) {
            return 0;
        }
        if (this instanceof OuterProgressConditionHandler handler) {
            return handler.getCurrentStateProgress(player, cfgCondition);
        }
        return countingProgress(player, cfgCondition);
    }

    /**
     * 获取条件变化事件改变的进度
     */
    public int getEventDeltaProgress(Player player, CfgCondition cfgCondition, PlayerEvent.ConditionChangeEvent event) {
        if (cfgCondition.getSourceType() != 1) {
            return 0;
        }
        if (this instanceof OuterProgressConditionHandler handler) {
            return handler.getCurrentStateProgress(player, cfgCondition);
        }
        if (isMatch(cfgCondition.getConditionParams(),  (i) -> event.params().get(i))) {
            return event.count();
        }
        return 0;
    }

    /**
     * 累积型条件类型，从历史记录数据中匹配获取
     */
    private int countingProgress(Player player, CfgCondition cfgCondition) {
        TaskOriginalData taskOriginalData = player.getPlayerData().getPlayerTaskDataMap().get(getType());
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

    /**
     * 判断事件的条件参数是否匹配
     *
     * @param conditionParams   配置表中配置的条件参数
     * @param paramFunction     从当前事件或历史数据中获取条件对应索引的参数值
     * @return                  是否匹配
     */
    private boolean isMatch(List<Entry.Int2IntVal> conditionParams, ToIntFunction<Integer> paramFunction) {
        if (conditionParams.isEmpty()) {
            return true;
        }

        for (int i = 0, size = conditionParams.size(); i < size; i++) {
            Entry.Int2IntVal int2IntVal = conditionParams.get(i);
            int conditionParam = paramFunction.applyAsInt(i);
            // 用简单数字表示大小关系，-2表示小于，-1表示小于等于，0表示等于，1表示大于等于，2表示大于
            boolean isMatch = switch (int2IntVal.k()) {
                case -2 -> conditionParam < int2IntVal.v();
                case -1 -> conditionParam <= int2IntVal.v();
                case 0 -> conditionParam == int2IntVal.v();
                case 1 -> conditionParam >= int2IntVal.v();
                case 2 -> conditionParam > int2IntVal.v();
                default -> false;
            };
            if (!isMatch) {
                return false;
            }
        }
        return true;
    }

}
