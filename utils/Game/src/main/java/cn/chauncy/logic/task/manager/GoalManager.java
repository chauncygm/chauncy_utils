package cn.chauncy.logic.task.manager;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.event.ConditionEvent;
import cn.chauncy.event.PlayerEvent;
import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.player.component.GoalComponent;
import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.logic.task.GoalData;
import cn.chauncy.logic.task.TaskData;
import cn.chauncy.logic.task.TaskOriginalData;
import cn.chauncy.logic.task.handler.*;
import cn.chauncy.template.bean.CfgCondition;
import cn.chauncy.utils.eventbus.Subscribe;
import cn.chauncy.utils.stuct.Pair;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 任务目标管理器
 */
public class GoalManager {

    private static final Logger logger = LoggerFactory.getLogger(GoalManager.class);

    /** 所有的条件处理器Map */
    private final Map<ConditionType, ConditionHandler> handlers = new IdentityHashMap<>();
    /** 外部获取进度的条件类型集合 */
    private final Set<ConditionType> outerProgressiveType = new HashSet<>();

    private final GlobalEventBus globalEventBus;

    @Inject
    public GoalManager(GlobalEventBus globalEventBus) {
        this.globalEventBus = globalEventBus;

        initRegisterHandlers();
    }

    /**
     * 初始化，注册所有条件处理器
     */
    private void initRegisterHandlers() {
        registerHandler(new NoneHandler());
        registerHandler(new LoginDaysHandler());
        registerHandler(new OnlineTimeHandler());
        registerHandler(new GetItemHandler());
        registerHandler(new SpendItemHandler());
        registerHandler(new FinishTaskHandler());
    }

    /**
     * 注册条件处理器
     */
    private void registerHandler(ConditionHandler handler) {
        handlers.put(handler.getType(), handler);
        if (handler.isOuterProgress()) {
            outerProgressiveType.add(handler.getType());
        }
    }

    /**
     * 更新所有注册的任务目标进度
     */
    public void updateAllGoal(Player player) {
        for (Map.Entry<ConditionType, List<GoalComponent.Goal>> entry : player.getGoalComponent().getGoals().entrySet()) {
            ConditionType conditionType = entry.getKey();
            List<GoalComponent.Goal> goalList = entry.getValue();
            if (!outerProgressiveType.contains(conditionType)) {
                continue;
            }
            ConditionHandler handler = handlers.get(conditionType);
            for (GoalComponent.Goal goal : goalList) {
                updateGoal(player, handler, goal, null);
            }
        }
    }

    /**
     * 注册任务目标
     *
     * @param immediateUpdate   是否立即刷新进度，接取单个任务时一般需要立即刷新
     */
    public void registerTaskGoal(Player player, TaskData taskData, boolean immediateUpdate) {
        int taskId = taskData.getConfigId();
        List<GoalData> goalList = taskData.getGoalList();
        for (GoalData goalData : goalList) {
            GoalComponent.Goal goal = new GoalComponent.Goal(taskData.getConfigId(), goalData);

            CfgCondition cfgCondition = CfgCondition.get(goal.getData().getGoalId());
            if (cfgCondition == null) {
                logger.error("condition not exist, goalId: {}", goal.getData().getGoalId());
                continue;
            }
            ConditionType conditionType = ConditionType.valueOf(cfgCondition.getConditionType());
            if (!handlers.containsKey(conditionType)) {
                logger.error("register task goal but condition handler not exist, type: {}", conditionType);
                continue;
            }

            Pair<Integer, Integer> taskGoalKey = Pair.of(taskId, goal.getData().getGoalId());
            if (player.getGoalComponent().getGoalMap().containsKey(taskGoalKey)) {
                logger.error("goal already exist, taskId: {}, goalId: {}", taskId, goal.getData().getGoalId());
                continue;
            }

            player.getGoalComponent().getGoalMap().put(taskGoalKey, goal);
            Map<ConditionType, List<GoalComponent.Goal>> goals = player.getGoalComponent().getGoals();
            if (!goals.containsKey(conditionType)) {
                goals.put(conditionType, new ArrayList<>());
            }
            goals.get(conditionType).add(goal);

            if (immediateUpdate && outerProgressiveType.contains(conditionType)) {
                ConditionHandler conditionHandler = handlers.get(conditionType);
                updateGoal(player, conditionHandler, goal, null);
            }
        }
    }

    /**
     * 移除任务目标
     */
    public void removeTaskGoal(Player player, TaskData taskData) {
        int taskId = taskData.getConfigId();
        GoalComponent goalComponent = player.getGoalComponent();

        List<GoalData> goalList = taskData.getGoalList();
        for (GoalData goalData : goalList) {
            int goalId = goalData.getGoalId();
            Pair<Integer, Integer> taskGoalKey = Pair.of(taskId, goalId);
            goalComponent.getGoalMap().remove(taskGoalKey);

            CfgCondition cfgCondition = CfgCondition.get(goalId);
            if (cfgCondition == null) {
                logger.warn("condition not exist when removing goal, goalId: {}", goalId);
                continue;
            }

            ConditionType conditionType = ConditionType.valueOf(cfgCondition.getConditionType());
            List<GoalComponent.Goal> goals = goalComponent.getGoals().get(conditionType);
            if (goals != null) {
                goals.removeIf(goal -> goal.getTaskId() == taskId && goal.getData().getGoalId() == goalId);
            }
        }
    }

    @Subscribe
    public void onConditionChangeEvent(ConditionEvent event) {
        PlayerEvent.ConditionChangeEvent changeEvent = event.convert();
        if (changeEvent.count() <= 0) {
            throw new IllegalArgumentException("ConditionChangeEvent count <= 0");
        }

        Player player = changeEvent.player();
        ConditionType conditionType = changeEvent.conditionType();

        ConditionHandler conditionHandler = handlers.get(conditionType);
        if (conditionHandler == null) {
            logger.error("trigger event but condition handler not exist, type: {}", conditionType);
            return;
        }

        saveTaskEventData(player, changeEvent);

        List<GoalComponent.Goal> goals = player.getGoalComponent().getGoals().get(conditionType);
        if (goals != null && !goals.isEmpty()) {
            for (GoalComponent.Goal goal : goals) {
                updateGoal(player, conditionHandler, goal, changeEvent);
            }
        }

    }

    /**
     * 保存任务事件数据
     */
    private void saveTaskEventData(Player player, PlayerEvent.ConditionChangeEvent event) {
        Map<ConditionType, TaskOriginalData> taskDataMap = player.getPlayerData().getPlayerTaskDataMap();
        ConditionType conditionType = event.conditionType();

        TaskOriginalData taskOriginalData = taskDataMap.computeIfAbsent(conditionType, k -> new TaskOriginalData(conditionType));
        int[] params = event.params().stream().mapToInt(Integer::intValue).toArray();
        TaskOriginalData.ConditionData conditionKey = new TaskOriginalData.ConditionData(params);
        taskOriginalData.getDataList().merge(conditionKey, event.count(), Integer::sum);
    }

    /**
     * 更新任务目标进度
     */
    private void updateGoal(Player player, ConditionHandler conditionHandler,
                            GoalComponent.Goal goal, PlayerEvent.ConditionChangeEvent changeEvent) {
        int lastProgress = goal.getData().getProgress();
        CfgCondition cfgCondition = CfgCondition.get(goal.getData().getGoalId());

        int progress;
        if (cfgCondition.getSourceType() == 0) {
            // 获取条件当前进度
            progress = conditionHandler.getCurrentProgress(player, cfgCondition);
        } else {
            // 获取事件导致条件变化的进度
            int deltaProgress = conditionHandler.getEventDeltaProgress(player, cfgCondition, changeEvent);
            progress = lastProgress + deltaProgress;
        }
        if (progress != lastProgress) {
            globalEventBus.post(new PlayerEvent.TaskChangeEvent(player, goal.getTaskId(), goal.getData()));
        }
    }

    /**
     * 判断条件是否满足
     */
    public boolean reachCondition(Player player, int conditionId) {
        CfgCondition cfgCondition = CfgCondition.get(conditionId);
        if (cfgCondition == null) {
            return false;
        }
        ConditionType conditionType = ConditionType.valueOf(cfgCondition.getConditionType());
        ConditionHandler handler = handlers.get(conditionType);
        if (handler == null) {
            return false;
        }
        return handler.getCurrentProgress(player, cfgCondition) >= cfgCondition.getTarget();
    }

}
