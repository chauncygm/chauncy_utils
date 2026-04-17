package cn.chauncy.behavior_tree.leaf;

import java.util.Random;

/**
 * 随机节点，50%成功或失败
 */
public class RandomNode extends LeafNode {

    private final Random random;

    public RandomNode(Random random) {
        this.random = random;
    }
    @Override
    protected void execute() {
        if (random.nextFloat() < 0.5) {
            setSuccess();
        } else {
            setFailure();
        }
    }

    @Override
    protected void onEventImpl(Object event) {

    }
}
