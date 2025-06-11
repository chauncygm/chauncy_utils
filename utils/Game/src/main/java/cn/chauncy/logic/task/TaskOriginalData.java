package cn.chauncy.logic.task;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.IdentityHashMap;
import java.util.Map;

@Data
public class TaskOriginalData {

    private ConditionType conditionType;

    private Map<ConditionData, Integer> dataList = new IdentityHashMap<>();

    @EqualsAndHashCode
    @Data
    public static class ConditionData {

        /** 任务条件参数，最多5个 */
        private int[] params;
    }
}
