package cn.chauncy.logic.task.manager;

import cn.chauncy.dao.entity.PlayerData;
import cn.chauncy.event.PlayerEvent;
import cn.chauncy.exception.ConfigErrorException;
import cn.chauncy.logic.bag.manager.BagManager;
import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.task.*;
import cn.chauncy.template.CfgTips;
import cn.chauncy.template.bean.CfgCondition;
import cn.chauncy.template.bean.CfgTask;
import cn.chauncy.utils.eventbus.Subscribe;
import cn.chauncy.utils.time.TimeProvider;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class TaskManager {

    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private final GoalManager goalManager;
    private final BagManager bagManager;
    private final TimeProvider timeProvider;

    @Inject
    public TaskManager(GoalManager goalManager, BagManager bagManager, TimeProvider timeProvider) {
        this.goalManager = goalManager;
        this.bagManager = bagManager;
        this.timeProvider = timeProvider;
    }

    public void init(Player player) {
        Map<Integer, TaskData> taskMap = player.getPlayerData().getTaskMap();
        for (TaskData taskData : taskMap.values()) {
            goalManager.registerTaskGoal(player, taskData, false);
        }
        goalManager.updateAllGoal(player);
    }

    private TaskData createTaskData(int taskId) {
        CfgTask cfgTask = CfgTask.get(taskId);
        if (cfgTask == null) {
            throw new ConfigErrorException("CfgTask", taskId, "not exist");
        }
        if (cfgTask.getGoalList().isEmpty()) {
            throw new ConfigErrorException("CfgTask", taskId, "task goal list is empty");
        }
        TaskData taskData = new TaskData();
        taskData.setConfigId(taskId);
        taskData.setTaskType(TaskType.valueOf(cfgTask.getType()));
        taskData.setTaskStatus(TaskStatus.FETCHED.getValue());
        taskData.setGoalList(new ArrayList<>(cfgTask.getGoalList().size()));
        for (int goalId : cfgTask.getGoalList()) {
            taskData.getGoalList().add(new GoalData(goalId));
        }
        taskData.setAcceptTime(timeProvider.getTimeMillis());
        taskData.setLastUpdateTime(taskData.getAcceptTime());
        return taskData;
    }

    public TaskData getTask(Player player, int taskId) {
        return player.getPlayerData().getTaskMap().get(taskId);
    }

    public TaskData acceptTask(Player player, int taskId) {
        if (!checkCanAccept(player, taskId)) {
            return null;
        }

        Map<Integer, TaskData> taskMap = player.getPlayerData().getTaskMap();
        TaskData taskData = taskMap.get(taskId);
        if (taskData == null) {
            taskData = createTaskData(taskId);
            taskMap.put(taskId, taskData);
        } else {
            throw new IllegalArgumentException("can't accept exist task.");
        }
        goalManager.registerTaskGoal(player, taskData, true);
        return taskData;
    }

    private boolean checkCanAccept(Player player, int taskId) {
        CfgTask cfgTask = CfgTask.get(taskId);
        if (cfgTask == null) {
            return false;
        }

        Set<Integer> finishedTaskIdSet = player.getPlayerData().getFinishedTaskIdSet();
        if (finishedTaskIdSet.contains(taskId)) {
            return false;
        }

        TaskData taskData = getTask(player, taskId);
        if (taskData != null) {
            return false;
        }

        int condition = cfgTask.getCondition();
        return condition <= 0 || goalManager.reachCondition(player, condition);
    }

    public void submitTask(Player player, int taskId) {
        TaskData taskData = getTask(player, taskId);
        if (taskData == null || taskData.getTaskStatus() != TaskStatus.FINISHED.getValue()) {
            return;
        }
        taskData.setTaskStatus(TaskStatus.SUBMITTED.getValue());
        CfgTask cfgTask = CfgTask.get(taskId);

        CfgTips cfgTips = bagManager.rewardItemList(player, cfgTask.getReward());
        if (cfgTips != CfgTips.SUCCESS_0) {
            logger.error("task reward fail, tips: {}, taskId: {}", cfgTips.getContent(), taskId);
        }

        PlayerData playerData = player.getPlayerData();
        playerData.getTaskMap().remove(taskId);
        playerData.getFinishedTaskIdSet().add(taskId);

        notifyTaskRemove(player, taskData);
    }

    public void giveUpTask(Player player, int taskId) {
        Map<Integer, TaskData> taskMap = player.getPlayerData().getTaskMap();
        TaskData taskData = taskMap.remove(taskId);
        if (taskData != null) {
            goalManager.removeTaskGoal(player, taskData);
            notifyTaskRemove(player, taskData);
        }
    }

    @Subscribe
    public void onTaskChangeEvent(PlayerEvent.TaskChangeEvent event) {
        Player player = event.player();
        int taskId = event.taskId();
        GoalData goalData = event.goalData();

        TaskData taskData = getTask(player, taskId);
        if (taskData == null || taskData.getTaskStatus() >= TaskStatus.SUBMITTED.getValue()) {
            return;
        }

        int finishSize = 0;
        CfgTask cfgTask = CfgTask.get(taskId);
        int goalSize = cfgTask.getGoalList().size();
        for (GoalData data : taskData.getGoalList()) {
            CfgCondition cfgCondition = CfgCondition.get(data.getGoalId());
            if (cfgCondition == null) {
                throw new ConfigErrorException("CfgCondition", data.getGoalId(), "not exist");
            }
            finishSize += data.getProgress() >= cfgCondition.getTarget() ? 1 : 0;
        }

        int needSize = Math.min(cfgTask.getGroupType(), goalSize);
        if (finishSize >= needSize) {
            taskData.setTaskStatus(TaskStatus.FINISHED.getValue());
            goalManager.removeTaskGoal(player, taskData);
        }

        notifyTaskChange(player, taskData);
    }

    private void notifyTaskChange(Player player, TaskData taskData) {
        // notify
    }

    private void notifyTaskRemove(Player player, TaskData taskData) {
        // notify
    }

}
