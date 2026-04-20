package cn.chauncy.behavior_tree.branch;

import cn.chauncy.behavior_tree.Node;
import cn.chauncy.behavior_tree.leaf.FSMStateNode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 状态机节点
 * 子节点只能是状态节点(叶子节点)
 */
public class FSMNode extends SwitcherNode {

    /** 缓存所有状态，需确保是同一组状态，不包含重复的状态，保证状态是单例的 */
    private transient final Map<Class<?>, FSMStateNode> stateMap = new HashMap<>();

    private transient FSMStateNode nextState;

    @JsonCreator
    public FSMNode(@JsonProperty("children") List<FSMStateNode> children) {
        super(Collections.unmodifiableList(children));
        for (FSMStateNode state : children) {
            stateMap.put(state.getClass(), state);
        }
    }

    public FSMNode(FSMStateNode... children) {
        super(children);
        for (FSMStateNode state : children) {
            stateMap.put(state.getClass(), state);
        }
    }

    public void changeState(Class<?> clazz) {
        FSMStateNode newState = null;
        if (clazz != null) {
            newState = getState(clazz);
            if (!children.contains(newState)) {
                throw new IllegalStateException("Invalid state: " + newState);
            }
        }
        if (runningNode == newState) {
            throw new IllegalStateException("Already in state: " + newState);
        }
        if (nextState != null) {
            throw new IllegalStateException("change State is not complete: " + newState);
        }
        if (runningNode == null) {
            runningNode = newState;
        } else {
            nextState = newState;
            if (runningNode.isRunning()) {
                runningNode.setSuccess();
            }
        }

    }

    public FSMStateNode getState(Class<?> clazz) {
        FSMStateNode state = stateMap.get(clazz);
        if (state == null) {
            throw new NullPointerException("Not found state instance: " + clazz);
        }
        return state;
    }

    @Override
    protected void onEnter() {
        if (runningNode == null) {
            runningNode = children.get(0);
        }
    }

    @Override
    protected boolean absentResult() {
        return true;
    }

    @Override
    protected Node switchStrategy() {
        FSMStateNode state = nextState;
        nextState = null;
        return state;
    }

    @Override
    public void reset() {
        super.reset();
        nextState = null;
    }
}
