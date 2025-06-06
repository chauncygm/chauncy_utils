package cn.chauncy.logic.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Task {

    private static final int[] EMPTY_PROGRESS = new int[0];

    /** 配置id */
    private int configId;

    /** 任务类型，读取配置 */
    @JsonIgnore
    private TaskType taskType;

    /** 任务状态 */
    private TaskStatus taskStatus;

    /** 任务进度 */
    private int progress;

    /** 多条件任务子任务进度 */
    private int[] subTaskProgress = EMPTY_PROGRESS;

    /** 接取时间 */
    private long acceptTime;

    /** 上次更新时间 */
    private long lastUpdateTime;

}
