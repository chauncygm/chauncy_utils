package cn.chauncy.logic.task.manager;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.event.ConditionEvent;
import cn.chauncy.event.PlayerEvent;
import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.player.component.GoalComponent;
import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.logic.task.GoalData;
import cn.chauncy.logic.task.TaskData;
import cn.chauncy.logic.task.handler.ConditionHandler;
import cn.chauncy.logic.task.handler.LevelUpHandler3;
import cn.chauncy.template.bean.CfgCondition;
import cn.chauncy.utils.eventbus.Subscribe;
import cn.chauncy.utils.stuct.Pair;
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

    public GoalManager(GlobalEventBus globalEventBus) {
        this.globalEventBus = globalEventBus;

        initRegisterHandlers();
    }

    /**
     * 初始化注册条件处理器
     */
    private void initRegisterHandlers() {
        registerHandler(new LevelUpHandler3());
    }

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
                logger.error("condition handler not exist, type: {}", conditionType);
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
        Player player = changeEvent.player();
        ConditionType conditionType = changeEvent.conditionType();
        ConditionHandler conditionHandler = handlers.get(conditionType);
        if (conditionHandler == null) {
            logger.error("condition handler not exist, type: {}", conditionType);
            return;
        }


        List<GoalComponent.Goal> goals = player.getGoalComponent().getGoals().get(conditionType);
        if (goals == null || goals.isEmpty()) {
            return;
        }
        for (GoalComponent.Goal goal : goals) {
            if (goal == null) {
                logger.error("goal is null");
                continue;
            }
            updateGoal(player, conditionHandler, goal, changeEvent);
        }
    }

    private void updateGoal(Player player, ConditionHandler conditionHandler, GoalComponent.Goal goal, PlayerEvent.ConditionChangeEvent changeEvent ) {
        boolean update = conditionHandler.updateProgress(player, goal, changeEvent);
        if (update) {
            globalEventBus.post(new PlayerEvent.TaskChangeEvent(player, goal.getTaskId(), goal.getData()));
        }
    }

}
