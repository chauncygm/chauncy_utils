package cn.chauncy.behavior_tree.decorate;

import cn.chauncy.behavior_tree.Node;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 装饰节点
 * 装饰器模式，对子节点进行装饰
 */
public abstract class DecorateNode extends Node {

    protected Node child;

    @JsonCreator
    public DecorateNode(@JsonProperty("child") Node child) {
        if (child != null) {
            addChild(child);
        }
    }

    @Override
    protected void execute() {
        executeNode(child);
    }

    @Override
    protected void onChildComplete(Node child) {
        setComplete(child.getStatus());
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int indexOf(Node child) {
        return child == this.child ? 0 : -1;
    }

    @Override
    public Node getChild(int index) {
        return index == 0 ? child : null;
    }

    @Override
    public void addChildImpl(Node child) {
        if (this.child == null) {
            this.child = child;
            child.setParent(this);
        } else {
            throw new UnsupportedOperationException("DecorateNode can only have one child.");
        }
    }

    @Override
    protected void setChildImpl(int index, Node child) {
        if (index == 0) {
            this.child = child;
        }
    }

    @Override
    public void removeChildImpl(int index) {
        if (index == 0) {
            child = null;
        }
    }

    @Override
    protected void onEventImpl(Object event) {
        if (child != null) {
            child.onEvent(event);
        }
    }
}
