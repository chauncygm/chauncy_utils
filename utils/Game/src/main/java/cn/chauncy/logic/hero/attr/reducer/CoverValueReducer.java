package cn.chauncy.logic.hero.attr.reducer;


import cn.chauncy.logic.hero.attr.type.MergeType;

public class CoverValueReducer extends ValueReducer {

    private int index = -1;

    @Override
    public MergeType getType() {
        return MergeType.COVER;
    }

    @Override
    public void merge(int value) {
        resources.add(value);
        index = resources.size() - 1;
    }

    @Override
    public void unmerge(int value) {
        if (resources.isEmpty() || resources.get(index) != value) {
            return;
        }
        resources.remove(index);
        index = resources.size() - 1;
    }

    @Override
    public void mul(int factor) {
        for (int i = resources.size() - 1; i >= 0; i--) {
            resources.set(i, resources.get(i) * factor);
        }
    }

    @Override
    public int getValue() {
        return index >= 0 ? resources.get(index) : 0;
    }
}
