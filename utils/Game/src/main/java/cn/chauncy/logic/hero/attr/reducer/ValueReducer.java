package cn.chauncy.logic.hero.attr.reducer;

import cn.chauncy.logic.hero.attr.type.MergeType;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.List;

public abstract class ValueReducer {
    protected List<Integer> resources = new IntArrayList(4);

    public abstract MergeType getType();

    public abstract void merge(int value);

    public abstract void unmerge(int value);

    public abstract void mul(int factor);

    public abstract int getValue();

}
