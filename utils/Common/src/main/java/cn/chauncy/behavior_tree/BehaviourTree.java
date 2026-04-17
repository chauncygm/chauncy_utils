package cn.chauncy.behavior_tree;

public class BehaviourTree {

    private final String name;
    private final Node root;

    public BehaviourTree(String name, Node root) {
        this.name = name;
        this.root = root;
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
