package cn.chauncy.logic.function;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.event.PlayerEvent;
import cn.chauncy.logic.player.Player;
import com.google.inject.Inject;

/** 功能开启管理器 */
public class FunctionManager {

    private final GlobalEventBus globalEventBus;

    @Inject
    public FunctionManager(GlobalEventBus globalEventBus) {
        this.globalEventBus = globalEventBus;
    }

    public void checkAllFunction(Player player) {

    }

    public void openFunction(Player player, int functionId) {
        globalEventBus.post(new PlayerEvent.FunctionOpenEvent(player, functionId));
    }

    public boolean isFunctionOpen(Player player, int functionId) {
        return false;
    }

}
