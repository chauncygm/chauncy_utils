package cn.chauncy.logic.hero.attr.reducer;


import cn.chauncy.logic.hero.attr.type.MergeType;

/**
 * 一定是万分比
 */
public class MultiplyValueReducer extends ValueReducer {

    private float value;

    @Override
    public MergeType getType() {
        return MergeType.MUL;
    }

    @Override
    public void merge(int value) {
        if (value <= 0) {
            return;
        }

        float val = 1 + value / 10000f;
        resources.add(value);
        if (this.value == 0) {
            this.value = val;
            return;
        }
        this.value *= val;
    }

    @Override
    public void unmerge(int value) {
        if (resources.remove((Integer) value)) {
            float val = 1 + value / 10000f;
            this.value /= val;
        }
    }

    @Override
    public void mul(int factor) {
        this.value *= factor;
    }

    @Override
    public int getValue() {
        return (int) (value * 10000);
    }
}
