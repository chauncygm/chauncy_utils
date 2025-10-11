package cn.chauncy.logic.hero.attr;

import cn.chauncy.logic.hero.attr.type.AttrType;

public class Attr {

    private final int[] attrs;

    public Attr() {
        attrs = new int[AttrType.COUNT];
    }

    public Attr(AttrReducer reducer) {
        attrs = new int[AttrType.COUNT];
        for (AttrType type : AttrType.values) {
            attrs[type.ordinal()] = reducer.getAttr(type.propId);
        }
    }

    public int getAttr(AttrType attrType) {
        return attrs[attrType.ordinal()];
    }
}
