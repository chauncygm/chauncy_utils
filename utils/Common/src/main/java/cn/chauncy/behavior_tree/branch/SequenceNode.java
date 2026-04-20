package cn.chauncy.behavior_tree.branch;

import cn.chauncy.behavior_tree.Node;
import cn.chauncy.behavior_tree.Status;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * 顺序执行节点
 * 通过stopHandler来表示与逻辑/或逻辑/Foreach逻辑/
 */
public class SequenceNode extends SingleRunningNode {

    protected int mode;
    /** 当前运行的节点索引 */
    protected transient int runningIndex;
    private transient StopHandler stopHandler;

    @JsonCreator
    public SequenceNode(@JsonProperty("children") List<Node> children,
                        @JsonProperty("mode") int mode) {
        super(children);
        this.mode = mode;
        initStopHandler();
    }

    public SequenceNode(Node... children) {
        this(0, children);
    }

    public SequenceNode(int mode, Node... children) {
        super(children);
        this.mode = mode;
        initStopHandler();
    }

    private void initStopHandler() {
        if (mode == 0) {
            stopHandler = AllCompleteStopHandler.INSTANCE;
        } else if (mode > 0) {
            stopHandler = OneFailureStopHandler.INSTANCE;
        } else{
            stopHandler = OneSuccessStopHandler.INSTANCE;
        }
    }

    @Override
    protected void onEnter() {
        super.onEnter();
        runningIndex = -1;
    }

    @Override
    protected void execute() {
        if (runningNode != null && !runningNode.isComplete()) {
            executeNode(runningNode);
            return;
        }

        runningIndex++;
        if (runningIndex >= children.size()) {
            // 所有子节点已执行完毕，根据stopHandler决定最终状态
            setComplete(stopHandler.getStopStatus(runningNode));
            return;
        }

        runningNode = children.get(runningIndex);
        runningNode.reset();
        executeNode(runningNode);
    }

    @Override
    protected void onChildComplete(Node child) {
        if (child != runningNode) {
            throw new IllegalArgumentException("runningNode != child");
        }
        boolean lastNode = runningIndex == size() - 1;
        if (stopHandler.checkStop(lastNode, child)) {
            setComplete(stopHandler.getStopStatus(child));
        }
    }

    @Override
    protected void onExit() {
        super.onExit();
        runningIndex = -1;
    }

    @Override
    public void reset() {
        super.reset();
        runningIndex = -1;
        runningNode = null;
    }

    interface StopHandler {
        boolean checkStop(boolean lastNode, Node child);
        Status getStopStatus(Node child);
    }

    /**
     * 或逻辑，一个节点成功则停止并以成功结算，全部失败算失败
     */
    private static class OneSuccessStopHandler implements StopHandler {

        public static final OneSuccessStopHandler INSTANCE = new OneSuccessStopHandler();
        @Override
        public boolean checkStop(boolean lastNode, Node child) {
            return lastNode || child.isSuccess();
        }

        @Override
        public Status getStopStatus(Node child) {
            return child.getStatus() == Status.SUCCESS ? Status.SUCCESS : Status.FAILURE;
        }
    }

    /**
     * 与逻辑，一个节点失败则停止并以失败结算，全部成功算成功
     */
    private static class OneFailureStopHandler implements StopHandler {
        public static final OneFailureStopHandler INSTANCE = new OneFailureStopHandler();
        @Override
        public boolean checkStop(boolean lastNode, Node child) {
            return lastNode || child.isFailure();
        }

        @Override
        public Status getStopStatus(Node child) {
            return child.getStatus() == Status.FAILURE ? Status.FAILURE : Status.SUCCESS;
        }
    }

    /**
     * Foreach逻辑，所有节点执行完毕，以成功结算
     */
    private static class AllCompleteStopHandler implements StopHandler {
        public static final AllCompleteStopHandler INSTANCE = new AllCompleteStopHandler();
        @Override
        public boolean checkStop(boolean lastNode, Node child) {
            return lastNode;
        }
        @Override
        public Status getStopStatus(Node child) {
            return Status.SUCCESS;
        }
    }

}
