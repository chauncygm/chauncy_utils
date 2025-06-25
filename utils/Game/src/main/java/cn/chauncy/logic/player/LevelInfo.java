package cn.chauncy.logic.player;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class LevelInfo {
    private int level;
    private int exp;
    private Set<Integer> rewardSet = new HashSet<>();
}
