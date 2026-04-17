package cn.chauncy.behavior_tree.leaf;


import cn.chauncy.behavior_tree.branch.FSMNode;

/**
 * FSM状态叶节点
 */
public abstract class FSMStateNode extends LeafNode{

    public void changeState(Class<?> clazz) {
        if (!status.isRunning()) {
            throw new IllegalStateException("state node is not running.");
        }
        FSMNode fsmNode = (FSMNode) getParent();
        fsmNode.changeState(clazz);
    }

    @Override
    protected void onEventImpl(Object event) {

    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + status + "]";
    }

}
