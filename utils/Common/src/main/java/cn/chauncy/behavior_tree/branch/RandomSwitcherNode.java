package cn.chauncy.behavior_tree.branch;

import cn.chauncy.behavior_tree.Node;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Random;

/**
 * 随机切换节点
 */
public class RandomSwitcherNode extends SwitcherNode {

    private final long seed;
    private final transient Random random;

    @JsonCreator
    public RandomSwitcherNode(@JsonProperty("seed") long seed,
                              @JsonProperty("children") List<Node> children) {
        super(children);
        this.seed = seed;
        random = new Random(seed);
    }

    public RandomSwitcherNode(long seed, Node... children) {
        super(children);
        this.seed = seed;
        random = new Random(seed);
    }

    @Override
    protected Node switchStrategy() {
        int randomIndex = random.nextInt(children.size());
        return children.get(randomIndex);
    }
}
