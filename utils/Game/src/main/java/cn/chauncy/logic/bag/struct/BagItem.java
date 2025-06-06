package cn.chauncy.logic.bag.struct;

import lombok.Data;

@Data
public class BagItem {

    /** 背包位置索引 */
    private int position;

    private Item item;

    public BagItem(int position, Item item) {
        this.position = position;
        this.item = item;
    }
}
