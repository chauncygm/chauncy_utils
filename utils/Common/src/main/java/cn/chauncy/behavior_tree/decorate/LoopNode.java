package cn.chauncy.behavior_tree.decorate;

import cn.chauncy.behavior_tree.Node;

/**
 * 循环节点
 */
public abstract class LoopNode extends DecorateNode {

    private static final int DEFAULT_LOOP_MAX_COUNT = 10000;

    protected transient int loopMaxCount;
    protected transient int loopCount;

    public LoopNode(Node child) {
        super(child);
    }

    @Override
    protected void onEnter() {
        loopCount = 0;
        loopMaxCount = DEFAULT_LOOP_MAX_COUNT;
    }

    @Override
    protected void execute() {
        if (child.isComplete()) {
            child.reset();
        }
        executeNode(child);
    }

    @Override
    protected void onExit() {
        loopCount = 0;
        loopMaxCount = 0;
    }

    protected abstract boolean checkCondition(Node node);

    @Override
    protected void onChildComplete(Node child) {
        loopCount++;
        if (checkCondition(child)) {
            setSuccess();
        }
        if (loopCount > loopMaxCount) {
            setFailure();
        }
    }

    @Override
    public void reset() {
        super.reset();
        loopCount = 0;
    }
}
