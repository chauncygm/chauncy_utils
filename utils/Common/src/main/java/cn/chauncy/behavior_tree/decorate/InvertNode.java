package cn.chauncy.behavior_tree.decorate;

import cn.chauncy.behavior_tree.Node;

/**
 * 反转节点
 */
public class InvertNode extends DecorateNode {

    public InvertNode(Node child) {
        super(child);
    }

    @Override
    protected void onChildComplete(Node child) {
        setComplete(child.getStatus().invert());
    }
}
