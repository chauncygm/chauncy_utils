package cn.chauncy.logic.task.handler;

import cn.chauncy.base.Entry;
import cn.chauncy.event.PlayerEvent;
import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.player.component.GoalComponent;
import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.logic.task.GoalData;
import cn.chauncy.template.bean.CfgCondition;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

@Getter
public abstract class ConditionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(ConditionHandler.class);

    private final boolean isOuterProgress;

    public ConditionHandler(boolean isOuterProgress) {
        this.isOuterProgress = isOuterProgress;
    }

    public abstract ConditionType getType();

    /**
     * 更新任务目标进度
     *
     * @param player    玩家
     * @param goal      目标数据
     * @param event     当前触发的事件
     */
    public boolean updateProgress(Player player, GoalComponent.Goal goal, PlayerEvent.@Nullable ConditionChangeEvent event) {
        GoalData goalData = goal.getData();
        CfgCondition cfgCondition = CfgCondition.get(goalData.getGoalId());

        int currentProgress = goalData.getProgress();
        if (isOuterProgress) {
            int progress = ((OuterProgressConditionHandler) this).getCurrentStateProgress(player, cfgCondition);
            if (progress != currentProgress) {
                goal.getData().setProgress(progress);
                return true;
            }
        }

        Objects.requireNonNull(event);
        if (isMatch(cfgCondition.getConditionParams(),  (i) -> event.params().get(i))) {
            int deltaProgress = event.count();
            if (deltaProgress > 0) {
                goal.getData().setProgress(currentProgress + deltaProgress);
                return true;
            }
        }
        return false;
    }

    /**
     * 判断事件的条件参数是否匹配
     *
     * @param conditionParams   配置表中配置的条件参数
     * @param paramFunction     从当前事件或历史数据中获取条件对应索引的参数值
     * @return                  是否匹配
     */
    protected boolean isMatch(List<Entry.Int2IntVal> conditionParams, ToIntFunction<Integer> paramFunction) {
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
