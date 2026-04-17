package cn.chauncy.behavior_tree.leaf;

/**
 * 行为节点
 */
public abstract class ActionNode extends LeafNode {

    @Override
    protected void execute() {
        boolean success = executeImpl();
        if (success) {
            setSuccess();
        } else {
            setFailure();
        }
    }

    protected abstract boolean executeImpl();

}
