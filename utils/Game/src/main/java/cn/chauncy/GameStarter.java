package cn.chauncy;

import cn.chauncy.utils.thread.ConsoleThread;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.Set;


public class GameStarter {

    private final ServiceManager manager;

    @Inject
    public GameStarter(Set<Service> services) {
        this.manager = new ServiceManager(services);
    }

    public void start() {
        manager.startAsync();
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(GameModule.INSTANCE);

        GameStarter starter = injector.getInstance(GameStarter.class);
        starter.start();

        ConsoleThread.INSTANCE.addHandlerClass(ConsoleHandler.class).daemonStart();
    }
}
