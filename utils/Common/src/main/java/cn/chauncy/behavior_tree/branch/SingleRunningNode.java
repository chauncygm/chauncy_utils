package cn.chauncy.behavior_tree.branch;

import cn.chauncy.behavior_tree.Node;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 最多只有一个正在运行的子节点
 * 相对于并行节点
 */
public abstract class SingleRunningNode extends BranchNode {

    /** 当前运行的节点 */
    protected transient Node runningNode;

    @JsonCreator
    public SingleRunningNode(@JsonProperty("children") List<Node> children) {
        super(children);
    }

    public SingleRunningNode(Node... children) {
        super(children);
    }

    @Override
    protected void onEnter() {
        runningNode = null;
    }

    @Override
    protected void onEventImpl(Object event) {
        if (runningNode != null && !runningNode.isRunning()) {
            runningNode.onEvent(event);
        }
    }

    @Override
    protected void onExit() {
        runningNode = null;
    }

    @Override
    public void reset() {
        super.reset();
        runningNode = null;
    }
}
