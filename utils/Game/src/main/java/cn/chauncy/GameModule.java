package cn.chauncy;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.manager.PlayerManager;
import cn.chauncy.net.GameMessageDispatcher;
import cn.chauncy.net.GameMessageRegistry;
import cn.chauncy.services.NetService;
import cn.chauncy.utils.net.handler.MessageDispatcher;
import cn.chauncy.utils.net.proto.MessageRegistry;
import com.google.common.util.concurrent.Service;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class GameModule implements Module {

    public static final GameModule INSTANCE = new GameModule();

    private GameModule() {}

    @Override
    public void configure(Binder binder) {
        binder.requireExplicitBindings();

        binder.bind(PlayerManager.class).in(Singleton.class);

        Multibinder<Service> multibinder = Multibinder.newSetBinder(binder, Service.class);
        multibinder.addBinding().to(NetService.class);

        binder.bind(GameStarter.class).in(Singleton.class);
        binder.bind(GlobalEventBus.class).in(Singleton.class);
        binder.bind(MessageRegistry.class).to(GameMessageRegistry.class).in(Singleton.class);
        binder.bind(MessageDispatcher.class).to(GameMessageDispatcher.class).in(Singleton.class);

        binder.bind(PlayerManager.class).in(Singleton.class);
    }
}
