package cn.chauncy.logic;

import cn.chauncy.logic.bag.manager.BagManager;
import cn.chauncy.logic.bag.manager.ResourceManager;
import cn.chauncy.logic.function.FunctionManager;
import cn.chauncy.logic.player.PlayerManager;
import com.google.inject.Injector;

public class Managers {

    public ResourceManager resourceManager;
    public FunctionManager functionManager;
    public BagManager bagManager;
    public PlayerManager playerManager;

    public void inject(Injector injector) {
        resourceManager = injector.getInstance(ResourceManager.class);
        functionManager = injector.getInstance(FunctionManager.class);
        bagManager = injector.getInstance(BagManager.class);
        playerManager = injector.getInstance(PlayerManager.class);
    }

}
