package cn.chauncy.logic.hero.attr.reducer;


import cn.chauncy.logic.hero.attr.type.MergeType;

public class AddValueReducer extends ValueReducer {

    private int value;

    @Override
    public MergeType getType() {
        return MergeType.SUM;
    }

    @Override
    public void merge(int value) {
        resources.add(value);
        this.value += value;
    }

    @Override
    public void unmerge(int value) {
        if (resources.remove((Integer) value)) {
            this.value -= value;
        }
    }

    @Override
    public void mul(int factor) {
        for (int i = resources.size() - 1; i >= 0; i--) {
            resources.set(i, resources.get(i) * factor);
        }
        this.value *= factor;
    }

    public int getValue() {
        return value;
    }
}
