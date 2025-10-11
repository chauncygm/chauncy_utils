package cn.chauncy.logic.hero.attr.sys;

import cn.chauncy.logic.player.Player;

public class EquipAttrSys implements AttrSys {
    @Override
    public FuncAttrSys getFuncAttrSys() {
        return FuncAttrSys.EQUIP;
    }

    @Override
    public Result getAttr(Player player) {
        return null;
    }
}
