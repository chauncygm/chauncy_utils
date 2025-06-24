package cn.chauncy.event;

import cn.chauncy.logic.player.Player;
import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.logic.task.GoalData;

import java.util.List;

public class PlayerEvent {

    /** 玩家创角 */
    public record PlayerCreateRoleEvent(Player player) {}

    /** 玩家登录 */
    public record PlayerLoginEvent(Player player) {}

    /** 玩家上线，在登录之后 */
    public record PlayerOnlineEvent(Player player) {}

    /** 玩家离线 */
    public record PlayerOfflineEvent(Player player) {}

    /** 玩家等级变化 */
    public record MainLevelChangeEvent(Player player, int level) {}

    /** 玩家功能开启 */
    public record FunctionOpenEvent(Player player, int functionId) {}

    /** 玩家功能关闭 */
    public record FunctionCloseEvent(Player player, int functionId) {}

    /** 玩家条件变化，影响任务或其他监听条件的系统 */
    public record ConditionChangeEvent(Player player, ConditionType conditionType, List<Integer> params, int count) {}

    /** 任务目标数据变化 */
    public record TaskChangeEvent(Player player, int taskId, GoalData  goalData) {}
}
