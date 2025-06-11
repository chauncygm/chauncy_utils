package cn.chauncy.logic.task;

import lombok.Data;

@Data
public class GoalData {

    public GoalData(int goalId) {
        this.goalId = goalId;
    }

    private int goalId;
    private int progress;
}
