package cn.chauncy.behavior_tree.branch;

import cn.chauncy.behavior_tree.Node;

import java.util.List;
import java.util.Random;

/**
 * 随机切换节点
 */
public class RandomSwitcherNode extends SwitcherNode {

    private final Random random;

    public RandomSwitcherNode(long seed, Node... children) {
        super(children);
        random = new Random(seed);
    }

    public RandomSwitcherNode(long seed, List<Node> children) {
        super(children);
        random = new Random(seed);
    }

    @Override
    protected Node switchStrategy() {
        int randomIndex = random.nextInt(children.size());
        return children.get(randomIndex);
    }
}
