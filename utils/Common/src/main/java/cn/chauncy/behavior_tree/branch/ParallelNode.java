package cn.chauncy.behavior_tree.branch;

import cn.chauncy.behavior_tree.Node;
import cn.chauncy.behavior_tree.Status;

/**
 * 并行执行节点
 */
public abstract class ParallelNode extends BranchNode {

    private transient int completeCount;

    @Override
    protected void onEnter() {
        completeCount = 0;
    }

    @Override
    protected void execute() {
        for (Node child : children) {
            if (!child.isComplete()) {
                executeNode(child);
            }
        }
    }

    @Override
    protected void onExit() {
        completeCount = 0;
    }

    @Override
    protected void onChildComplete(Node child) {
        completeCount++;
        if (completeCount >= children.size()) {
            setComplete(Status.SUCCESS);
        }
    }

    @Override
    protected void onEventImpl(Object event) {

    }
}
