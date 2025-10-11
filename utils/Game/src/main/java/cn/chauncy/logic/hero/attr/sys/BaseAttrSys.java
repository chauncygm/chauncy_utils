package cn.chauncy.logic.hero.attr.sys;

import cn.chauncy.logic.hero.attr.AttrReducer;
import cn.chauncy.logic.player.Player;

public class BaseAttrSys implements AttrSys {

    @Override
    public FuncAttrSys getFuncAttrSys() {
        return FuncAttrSys.BASE;
    }

    @Override
    public Result getAttr(Player player) {
        Result result = getTempResult();
        // 基础属性
        result.attrReducer.mergeOther(new AttrReducer());
        return result;
    }
}
