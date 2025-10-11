package cn.chauncy.logic.hero.attr.reducer;


import cn.chauncy.logic.hero.attr.type.MergeType;

public class MaxValueReducer extends ValueReducer {

    private int maxValue;

    @Override
    public MergeType getType() {
        return MergeType.MAX;
    }

    @Override
    public void merge(int value) {
        if (value <= 0) {
            return;
        }
        resources.add(value);
        if (value > maxValue) {
            maxValue = value;
        }
    }

    @Override
    public void unmerge(int value) {
        resources.remove((Integer) value);
        if (value == maxValue) {
            maxValue = resources.stream().max(Integer::compareTo).orElse(0);
        }
    }

    @Override
    public void mul(int factor) {
        for (int i = resources.size() - 1; i >= 0; i--) {
            resources.set(i, resources.get(i) * factor);
        }
        this.maxValue *= factor;
    }

    @Override
    public int getValue() {
        return maxValue;
    }
}
