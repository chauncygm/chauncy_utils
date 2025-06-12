package cn.chauncy.logic.task;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@Data
public class TaskOriginalData {

    private ConditionType conditionType;

    private Map<ConditionData, Integer> dataList = new HashMap<>();

    public TaskOriginalData(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    @Data
    @EqualsAndHashCode
    public static class ConditionData {

        public ConditionData(int[] params) {
            this.params = params;
        }

        /** 任务条件参数，最多5个 */
        private int[] params;
    }
}
