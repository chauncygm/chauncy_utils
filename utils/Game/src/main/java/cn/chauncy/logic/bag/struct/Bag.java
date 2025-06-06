package cn.chauncy.logic.bag.struct;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

@Data
public class Bag {

    /** 背包类型 */
    private BagType bagType;

    /** 背包大小 */
    private int bagSize;

    /** 背包道具, key道具guid，value背包道具 */
    private Map<Long, BagItem> bagItems = new HashMap<>();

    /** 空闲格子标记 */
    @JsonIgnore
    private BitSet positionBitSet = new BitSet();

}
