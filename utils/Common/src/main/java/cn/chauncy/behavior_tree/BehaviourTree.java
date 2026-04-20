package cn.chauncy.behavior_tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static cn.chauncy.behavior_tree.DefaultBlackboard.ENTITY_KEY;

public class BehaviourTree {

    @JsonProperty
    private final String name;
    @JsonProperty
    private final Node root;

    private transient Object entity;

    @JsonCreator
    public BehaviourTree(@JsonProperty("name") String name, @JsonProperty("root") Node root) {
        this.name = name;
        this.root = root;
        root.setBlackBoard(new DefaultBlackboard());
    }

    public void setEntity(Object entity) {
        this.entity = entity;
        root.getBlackBoard().set(ENTITY_KEY, entity);
    }

    public String getName() {
        return name;
    }

    public Object getEntity() {
        return entity;
    }

    public void update() {
        if (root == null) {
            throw new IllegalStateException("BehaviourTree root is null.");
        }
        if (root.isComplete()) {
            throw new IllegalStateException("BehaviourTree root has completed.");
        }
        Node.executeNode(root);
    }

    @Override
    public String toString() {
        return "BehaviourTree{" +
                "name='" + name + '\'' +
                ", root=" + root +
                '}';
    }
}
