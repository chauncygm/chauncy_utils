package cn.chauncy.behavior_tree.leaf;

/**
 * 等待固定时间节点
 */
public class WaitNode extends LeafNode {

    private final long waitTime;
    private transient long enterTime;

    public WaitNode(long waitTime) {
        this.waitTime = waitTime;
    }

    @Override
    protected void onEnter() {
        enterTime = System.currentTimeMillis();
    }

    @Override
    protected void execute() {
        long now = System.currentTimeMillis();
        if (now - enterTime >= waitTime) {
            setSuccess();
        }
    }

    @Override
    protected void onExit() {
        enterTime = 0;
    }

    @Override
    protected void onEventImpl(Object event) {

    }
}
