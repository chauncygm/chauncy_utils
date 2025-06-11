package cn.chauncy.logic.player.component;

import cn.chauncy.logic.task.ConditionType;
import cn.chauncy.logic.task.GoalData;
import cn.chauncy.utils.stuct.Pair;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class GoalComponent {

    /** 所有进行中的任务目标 */
    @Getter
    private final Map<ConditionType, List<Goal>> goals = new IdentityHashMap<>();

    @Getter
    private final Map<Pair<Integer, Integer>, Goal> goalMap = new HashMap<>();

    @Data
    public static class Goal {
        /** 关联的任务id */
        private int taskId;
        /** 关联的目标id */
        private GoalData data;

        public Goal(int taskId, GoalData data) {
            this.taskId = taskId;
            this.data = data;
        }
    }

}
