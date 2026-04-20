package cn.chauncy.behavior_tree.leaf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Random;

/**
 * 随机节点，50%成功或失败
 */
public class RandomNode extends LeafNode {

    private final long seed;
    private final transient Random random;

    @JsonCreator
    public RandomNode(@JsonProperty("seed") long send) {
        this.seed = send;
        this.random = new Random(send);
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
