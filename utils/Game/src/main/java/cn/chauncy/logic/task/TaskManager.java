package cn.chauncy.logic.task;

import cn.chauncy.logic.bag.manager.BagManager;
import com.google.inject.Inject;

public class TaskManager {

    private final BagManager bagManager;

    @Inject
    public TaskManager(BagManager bagManager) {
        this.bagManager  = bagManager;
    }

}
