package cn.chauncy.behavior_tree.decorate;

import cn.chauncy.behavior_tree.Node;

/**
 * 重复执行直到成功节点
 */
public class UntilSuccessNode extends LoopNode {

    public UntilSuccessNode(Node child) {
        super(child);
    }

    @Override
    protected boolean checkCondition(Node node) {
        return node.isSuccess();
    }

}
