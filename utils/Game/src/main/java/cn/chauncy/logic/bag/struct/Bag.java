package cn.chauncy.logic.bag.struct;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Bag {

    /** 背包类型 */
    private BagType bagType;

    /** 背包大小 */
    private int bagSize;

    /** 背包道具 */
    private Map<Integer, BagItem> bagItems = new HashMap<>();

}
