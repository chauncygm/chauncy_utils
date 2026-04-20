package cn.chauncy.behavior_tree;

import cn.chauncy.behavior_tree.leaf.GuardNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.chauncy.behavior_tree.DefaultBlackboard.ENTITY_KEY;

/**
 * 行为树节点抽象类
 * 生命周期 Status: NEW -> RUNNING -> SUCCESS/FAILURE
 * 执行流程 onEnter(第一次) -> execute(每帧执行) -> onExit(退出)
 *
 * @author Chauncy
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
public abstract class Node {

    protected static final Logger logger = LoggerFactory.getLogger(Node.class);

    /** 守卫节点，即条件测试节点，若不通过则无法执行此节点 */
    protected GuardNode guard;

    /** 父节点 */
    protected transient Node parent;
    /** 黑板数据 */
    protected transient Blackboard blackBoard;
    /** 运行状态 */
    protected transient Status status = Status.NEW;

    protected transient Object entity;

    public Object getEntity() {
        if (entity == null) {
            entity = blackBoard.get(ENTITY_KEY);
        }
        return entity;
    }

    public GuardNode getGuard() {
        return guard;
    }

    public void setGuard(GuardNode guard) {
        this.guard = guard;
    }

    public Blackboard getBlackBoard() {
        return blackBoard;
    }

    public void setBlackBoard(Blackboard blackBoard) {
        this.blackBoard = blackBoard;
    }

    public final Node getParent() {
        return parent;
    }

    public final void setParent(Node parent) {
        this.parent = parent;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    //region 生命周期行为
    /**
     * 节点第一次进入执行初始化逻辑，仅执行一次
     */
    protected void onEnter() {}

    /**
     * 节点执行逻辑，每帧执行
     */
    protected abstract void execute();

    /**
     * 节点退出,清理工作
     */
    protected void onExit() {}

    /**
     * 子节点完成事件
     * @param child 子节点
     */
    protected abstract void onChildComplete(Node child);

    /**
     * 收到外部事件
     * @param event 外部事件
     */
    public final void onEvent(Object event) {
        if (!isRunning()) {
            return;
        }
        onEventImpl(event);
    }

    protected abstract void onEventImpl(Object event);

    /**
     * 重置节点，清理数据
     */
    public void reset() {
        status = Status.NEW;
        if (guard != null) {
            guard.reset();
        }
        blackBoard = null;
    }
    //endregion


    //region 模板方法
    public static boolean testChildGuard(Node node) {
        GuardNode guard = node.guard;
        return guard == null || guard.checkGuard();
    }

    public static void executeNode(Node node) {
        if (node.isComplete()) {
            return;
        }

        try {
            if (node.status == Status.NEW) {
                if (node.blackBoard == null) {
                    node.blackBoard = node.parent == null ?
                            null : node.parent.getBlackBoard();
                }
                node.onEnter();
                if (node.isComplete()) {
                    return;
                }
            }
            node.status = Status.RUNNING;
            if (node.guard != null && !node.guard.checkGuard()) {
                node.setFailure();
                return;
            }
            node.execute();
        } catch (Exception e) {
            logger.error("execute node error.", e);
            node.setFailure();
        }
    }
    //endregion

    //region 子节点管理
    public abstract int size();

    /**
     * 获取子节点索引
     * @param child 子节点
     */
    public abstract int indexOf(Node child);

    /**
     * 获取子节点
     * @param index 索引
     */
    public abstract Node getChild(int index);

    /**
     * 添加子节点
     * @param child 子节点
     */
    public final void addChild(Node child) {
        checkAddChild(child);
        addChildImpl(child);
        child.setParent(this);
    }

    public final void setChild(int index, Node child) {
        if (index < 0 || index >= size()) {
            throw new IllegalArgumentException("index out of range. size: " + size() + ", index: " + index);
        }
        checkAddChild(child);
        setChildImpl(index, child);
    }

    protected abstract void addChildImpl(Node child);

    protected abstract void setChildImpl(int index, Node child);

    /**
     * 移除子节点
     * @param child 子节点
     */
    public final void removeChild(Node child) {
        int index = indexOf(child);
        if (index < 0) {
            throw new IllegalArgumentException("can not find child in node.");
        }
        removeChildImpl(index);
    }

    /**
     * 根据索引移除子节点
     * @param index 索引
     */
    public final void removeChild(int index) {
        if (index < 0 || index >= size()) {
            throw new IllegalArgumentException("index out of range. size: " + size() + ", index: " + index);
        }
        removeChildImpl(index);
    }

    protected abstract void removeChildImpl(int index);

    private void checkAddChild(Node child) {
        if (child == null) throw new IllegalArgumentException("child can not be null.");
        if (child == this) throw new IllegalArgumentException("child can not be self.");
        if (child.getParent() != null) throw new IllegalArgumentException("child already has parent node.");
        if (child.isRunning()) throw new IllegalArgumentException("child is running.");
    }
    //endregion

    //region 状态相关
    public void setSuccess() {
        postComplete(Status.SUCCESS);
    }

    public void setFailure() {
        postComplete(Status.FAILURE);
    }

    public void setComplete(Status status) {
        postComplete(status);
    }

    private void postComplete(Status status) {
        if (!isComplete(status)) {
            throw new IllegalArgumentException("complete with a wrong status: " + status);
        }
        Status preStatus = this.status;
        if (preStatus == Status.RUNNING) {
            this.status = status;

            if (parent != null) {
                parent.onChildComplete(this);
            }
            onExit();
        } else if (preStatus == Status.NEW) {
            throw new IllegalStateException("node is not running.");
        } else {
            throw new IllegalStateException("node is already complete.");
        }
    }

    public boolean isRunning() {
        return status == Status.RUNNING;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isFailure() {
        return status == Status.FAILURE;
    }

    public boolean isComplete() {
        return status == Status.SUCCESS || status == Status.FAILURE;
    }

    public boolean isComplete(Status status) {
        return status == Status.SUCCESS || status == Status.FAILURE;
    }
    //endregion
}
