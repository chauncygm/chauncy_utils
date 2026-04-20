package cn.chauncy.behavior_tree.branch;

import cn.chauncy.behavior_tree.Node;
import cn.chauncy.behavior_tree.Status;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 分支选择节点
 */
public class SwitcherNode extends SingleRunningNode {

    protected transient SwitchHandler handler = SwitchHandler.DEFAULT;

    @JsonCreator
    public SwitcherNode(@JsonProperty("children") List<Node> children) {
        super(children);
    }

    public SwitcherNode(Node... children) {
        super(children);
    }

    public SwitchHandler getHandler() {
        return handler;
    }

    public void setHandler(SwitchHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void execute() {
        if (runningNode != null && !runningNode.isComplete()) {
            executeNode(runningNode);
            return;
        }
        Node nextNode = switchStrategy();
        if (nextNode == null) {
            setComplete(absentResult() ? Status.SUCCESS : Status.FAILURE);
            return;
        }
        nextNode.reset();
        handler.onChangeState(runningNode, nextNode);
        runningNode = nextNode;
        executeNode(runningNode);
    }

    protected boolean absentResult() {
        return false;
    }

    protected Node switchStrategy() {
        for (Node child : children) {
            if (testChildGuard(child)) {
                return child;
            }
        }
        return null;
    }

    @Override
    protected void onChildComplete(Node child) {
        // do nothing
    }

    public interface SwitchHandler {

        SwitchHandler DEFAULT = new SwitchHandler() {};

        default void onChangeState(Node last, Node next) {
            logger.info("Change state: {} -> {}", last, next);
        }

        default void onFinale() {
            logger.info("Finale state");
        }
    }
}
