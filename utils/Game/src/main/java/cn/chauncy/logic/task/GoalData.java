package cn.chauncy.logic.task;

import lombok.Data;

@Data
public class GoalData {

    private int goalId;
    private int progress;

    public GoalData(int goalId) {
        this.goalId = goalId;
    }
}
