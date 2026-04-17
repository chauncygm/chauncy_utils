package cn.chauncy.behavior_tree.decorate;

import cn.chauncy.behavior_tree.Node;

/**
 * 重复执行直到失败节点
 */
public class UntilFailureNode extends LoopNode {

    public UntilFailureNode(Node child) {
        super(child);
    }

    @Override
    protected boolean checkCondition(Node node) {
        return node.isFailure();
    }

}
