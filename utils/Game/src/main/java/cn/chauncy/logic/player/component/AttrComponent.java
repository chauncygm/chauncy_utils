package cn.chauncy.logic.player.component;

import cn.chauncy.logic.hero.attr.Attr;
import cn.chauncy.logic.hero.attr.AttrReducer;

public class AttrComponent {

    private final AttrReducer basic = new AttrReducer();
    private final AttrReducer dynamic = new AttrReducer();

    private final AttrReducer finalAttr = new AttrReducer();

    public final Attr attr = new Attr();
}
