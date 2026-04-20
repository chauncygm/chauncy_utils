package cn.chauncy.behavior_tree.branch;

import cn.chauncy.behavior_tree.Node;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 分支节点
 */
public abstract class BranchNode extends Node {

    protected List<Node> children;

    public BranchNode() {
        this.children = new ArrayList<>();
    }

    @JsonCreator
    public BranchNode(@JsonProperty("children") List<Node> children) {
        this.children = children != null ? new ArrayList<>(children) : new ArrayList<>();
        for (Node child : this.children) {
            if (child != null) {
                child.setParent(this);
            }
        }
    }

    public BranchNode(Node... children) {
        this(Arrays.asList(children));
    }

    @Override
    protected void onEnter() {
        if (children.isEmpty()) {
            setFailure();
        }
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public int indexOf(Node child) {
        return children.indexOf(child);
    }

    @Override
    public Node getChild(int index) {
        return children.get(index);
    }

    @Override
    public void addChildImpl(Node child) {
        children.add(child);
    }

    @Override
    protected void setChildImpl(int index, Node child) {
        children.set(index, child);
    }

    @Override
    public void removeChildImpl(int index) {
        children.remove(index);
    }

    @Override
    public void reset() {
        super.reset();
        for (Node child : children) {
            child.reset();
        }
    }
}
