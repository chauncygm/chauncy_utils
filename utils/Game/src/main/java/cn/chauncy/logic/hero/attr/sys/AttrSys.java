package cn.chauncy.logic.hero.attr.sys;

import cn.chauncy.logic.hero.attr.AttrReducer;
import cn.chauncy.logic.player.Player;
import io.netty.util.concurrent.FastThreadLocal;

public interface AttrSys {

    /** 临时结果缓存 */
    FastThreadLocal<Result> tempResult = new FastThreadLocal<>();

    /** 属性系统类型 */
    FuncAttrSys getFuncAttrSys();

    /**  */
    Result getAttr(Player player);

    default Result getTempResult() {
        Result result = tempResult.get();
        result.clear();
        return result;
    }

    class Result {
        public AttrReducer attrReducer;
        public long combat;

        public Result(AttrReducer attrReducer, long combat) {
            this.attrReducer = attrReducer;
            this.combat = combat;
        }

        public void clear() {
            attrReducer.clear();
            combat = 0;
        }
    }
}
