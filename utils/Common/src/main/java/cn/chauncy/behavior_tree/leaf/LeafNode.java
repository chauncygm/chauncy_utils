package cn.chauncy.behavior_tree.leaf;

import cn.chauncy.behavior_tree.Node;

/**
 * 叶子节点
 */
public abstract class LeafNode extends Node {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int indexOf(Node child) {
        return -1;
    }

    @Override
    public Node getChild(int index) {
        throw new UnsupportedOperationException("LeafNode not have child.");
    }

    @Override
    public void addChildImpl(Node child) {
        throw new UnsupportedOperationException("LeafNode can not have child.");
    }

    @Override
    protected void setChildImpl(int index, Node child) {
        throw new UnsupportedOperationException("LeafNode can not have child.");
    }

    @Override
    public void removeChildImpl(int index) {
        throw new UnsupportedOperationException("LeafNode not have child.");
    }

    @Override
    protected void onChildComplete(Node child) {
        // never called
        throw new UnsupportedOperationException("LeafNode not have child!");
    }
}
