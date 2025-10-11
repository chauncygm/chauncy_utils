package cn.chauncy.logic.hero.attr.reducer;


import cn.chauncy.logic.hero.attr.type.MergeType;

public class MinValueReducer extends ValueReducer {

    private int minValue;

    @Override
    public MergeType getType() {
        return MergeType.MIN;
    }

    @Override
    public void merge(int value) {
        if (value <= 0) {
            return;
        }
        resources.add(value);
        if (value < minValue) {
            minValue = value;
        }
    }

    @Override
    public void unmerge(int value) {
        resources.remove((Integer) value);
        if (value == minValue) {
            minValue = resources.stream().min(Integer::compareTo).orElse(0);
        }
    }

    @Override
    public void mul(int factor) {
        for (int i = resources.size() - 1; i >= 0; i--) {
            resources.set(i, resources.get(i) * factor);
        }
        this.minValue *= factor;
    }

    @Override
    public int getValue() {
        return minValue;
    }
}
