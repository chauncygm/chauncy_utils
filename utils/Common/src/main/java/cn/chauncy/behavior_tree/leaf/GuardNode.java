package cn.chauncy.behavior_tree.leaf;

/**
 * 守卫(条件)节点
 * 用于测试条件是否满足
 */
public abstract class GuardNode extends LeafNode {

    @Override
    public void execute() {
        if (checkGuard()) {
            setSuccess();
        } else {
            setFailure();
        }
    }

    public abstract boolean checkGuard();
}
