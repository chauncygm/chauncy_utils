package cn.chauncy.logic.hero.attr;

import cn.chauncy.logic.hero.attr.sys.AttrSys;
import cn.chauncy.logic.hero.attr.sys.FuncAttrSys;
import cn.chauncy.logic.hero.unit.HeroUnit;
import cn.chauncy.logic.player.Player;

public class AttrManager {

    private AttrSys[] attrSys;

    public AttrManager(AttrSys[] attrSys) {
        this.attrSys = attrSys;
    }

    public void calculateAttr(Player player) {
        for (FuncAttrSys value : FuncAttrSys.values()) {

        }
    }

    public void calculateAttr(HeroUnit heroUnit) {

    }

}
