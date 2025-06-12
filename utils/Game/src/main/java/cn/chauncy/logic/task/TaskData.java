package cn.chauncy.logic.task;

import cn.chauncy.template.bean.CfgTask;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class TaskData {

    /** 配置id，同一id只能接取一次 */
    private int configId;

    /** 任务类型，读取配置 */
    @JsonIgnore
    private TaskType taskType;

    @JsonIgnore
    private Set<ConditionType> conditionType;

    @JsonIgnore
    private CfgTask cfgTask;

    /** 任务状态 */
    private int taskStatus;

    /** 任务进度，可包含多个条件的进度 */
    private List<GoalData> goalList;

    /** 接取时间 */
    private long acceptTime;

    /** 上次更新时间 */
    private long lastUpdateTime;

}
