package cn.chauncy.behavior_tree.decorate;

import cn.chauncy.behavior_tree.Node;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TimeoutNode extends DecorateNode {

    private final long timeoutMs;
    private transient long startTime;

    @JsonCreator
    public TimeoutNode(@JsonProperty("child") Node child,
                       @JsonProperty("timeoutMs") long timeoutMs) {
        super(child);
        this.timeoutMs = timeoutMs;
    }

    @Override
    protected void onEnter() {
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void execute() {
        long now = System.currentTimeMillis();
        if (now - startTime >= timeoutMs) {
            setFailure();
            return;
        }
        executeNode(child);
    }

    @Override
    protected void onExit() {
        startTime = 0;
    }
}
