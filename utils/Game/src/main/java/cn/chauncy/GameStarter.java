package cn.chauncy;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.event.SystemEvent;
import cn.chauncy.logic.Managers;
import cn.chauncy.logic.player.PlayerManager;
import cn.chauncy.util.ConsoleHandler;
import cn.chauncy.utils.thread.ConsoleService;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;


public class GameStarter {

    private static final Logger logger = LoggerFactory.getLogger(GameStarter.class);
    private final ServiceManager manager;
    private final GlobalEventBus eventBus;
    private final PlayerManager playerManager;

    @Inject
    public GameStarter(Set<Service> services, GlobalEventBus eventBus, PlayerManager playerManager) {
        this.manager = new ServiceManager(services);
        this.eventBus = eventBus;
        this.playerManager = playerManager;
    }

    public void start() {
        manager.startAsync();
    }

    public void waitSuccess() {
        manager.awaitHealthy();
        eventBus.post(new SystemEvent.ServerStartEvent());
        logger.info("服务器启动完成");
        playerManager.loadAllPlayerInfo();
    }

    public void waitStop() {
        logger.info("开始停服...");
        eventBus.post(new SystemEvent.ServerStopEvent());
        manager.stopAsync().awaitStopped();
        logger.info("服务器停服完成");
    }

    public static void main(String[] args) {
        ConsoleService.addHandlerClass(ConsoleHandler.class);
        try {
            CfgManager.INSTANCE.init();
        } catch (IOException e) {
            logger.error("初始化配置文件失败", e);
            System.exit(1);
        }

        Injector injector = Guice.createInjector(GameModule.INSTANCE);
        Managers managers = injector.getInstance(Managers.class);
        managers.inject(injector);

        GameStarter gameStarter = managers.gameStarter;
        gameStarter.start();
        gameStarter.waitSuccess();
        Runtime.getRuntime().addShutdownHook(new Thread(gameStarter::waitStop));

//        PlayerDataMapper instance = injector.getInstance(PlayerDataMapper.class);
//        int i = instance.deleteById(1001);
//        System.out.println(i);
//
//        CfgItem cfgItem = CfgItem.get(1001);
//        System.out.println(JsonUtils.toJson(cfgItem));
    }
}
