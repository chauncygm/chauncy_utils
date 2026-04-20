package cn.chauncy.behavior_tree;

import cn.chauncy.behavior_tree.branch.FSMNode;
import cn.chauncy.behavior_tree.leaf.FSMStateNode;

import java.util.List;

import static cn.chauncy.behavior_tree.DefaultBlackboard.ENTITY_KEY;

public class StateMachine {

    private final String name;
    private final FSMNode root;
    private transient Object entity;

    public StateMachine(String name, List<FSMStateNode> stateNodes) {
        this.name = name;
        this.root = new FSMNode(stateNodes);
    }

    public String getName() {
        return name;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
        root.getBlackBoard().set(ENTITY_KEY, entity);
    }

    public void update() {
        if (root.isComplete()) {
            throw new IllegalStateException("StateMachine root has completed.");
        }
        Node.executeNode(root);
    }

    public void changeState(Class<?> clazz) {
        root.changeState(clazz);
    }

    @Override
    public String toString() {
        return "StateMachine{" +
                "name='" + name + '\'' +
                ", root=" + root +
                '}';
    }
}
