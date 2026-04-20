package cn.chauncy.behavior_tree.decorate;


import cn.chauncy.behavior_tree.Node;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 重复执行N次节点
 */
public class RepeatNode extends LoopNode {

    private final int requireCount;

    @JsonCreator
    public RepeatNode(@JsonProperty("child") Node child,
                      @JsonProperty("requireCount") int requireCount) {
        super(child);
        this.requireCount = requireCount;
    }

    @Override
    protected void onEnter() {
        super.onEnter();
        if (requireCount > loopMaxCount) {
            loopMaxCount = requireCount;
        }
    }

    @Override
    protected boolean checkCondition(Node node) {
        return loopCount >= requireCount;
    }

    @Override
    public String toString() {
        return "RepeatNode{" +
                "requireCount=" + requireCount +
                ", loopCount=" + loopCount +
                '}';
    }
}
