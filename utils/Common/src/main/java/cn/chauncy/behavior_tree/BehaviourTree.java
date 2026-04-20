package cn.chauncy.behavior_tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BehaviourTree {

    @JsonProperty
    private final String name;
    @JsonProperty
    private final Node root;

    @JsonCreator
    public BehaviourTree(@JsonProperty("name") String name, @JsonProperty("root") Node root) {
        this.name = name;
        this.root = root;
    }

    public String getName() {
        return name;
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
