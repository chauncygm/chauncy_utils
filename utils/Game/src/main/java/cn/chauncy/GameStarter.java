package cn.chauncy;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.event.SystemEvent;
import cn.chauncy.logic.player.PlayerManager;
import cn.chauncy.util.ConsoleHandler;
import cn.chauncy.utils.thread.ConsoleService;
import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
import com.baomidou.mybatisplus.core.toolkit.reflect.TypeParameterResolver;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;


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
        // 临时代码修复防止报错
        fixMybatisPlusOnAbsenceSpringDependencyIssue();

        ConsoleService.addHandlerClass(ConsoleHandler.class);
        try {
            CfgManager.INSTANCE.init();
        } catch (IOException e) {
            logger.error("初始化配置文件失败", e);
            System.exit(1);
        }

        Injector injector = Guice.createInjector(GameModule.INSTANCE);
        GameStarter starter = injector.getInstance(GameStarter.class);
        starter.start();
        starter.waitSuccess();
        Runtime.getRuntime().addShutdownHook(new Thread(starter::waitStop));

//        PlayerDataMapper instance = injector.getInstance(PlayerDataMapper.class);
//        int i = instance.deleteById(1001);
//        System.out.println(i);
//
//        CfgItem cfgItem = CfgItem.get(1001);
//        System.out.println(JsonUtils.toJson(cfgItem));
    }

    private static void fixMybatisPlusOnAbsenceSpringDependencyIssue() {
        GenericTypeUtils.setGenericTypeResolver((Class<?> clazz, Class<?> genericIfc) -> {
            Map<TypeVariable<?>, Type> map = new HashMap<>();
            new TypeParameterResolver(map){}.visitType(clazz);
            List<Class<?>> result  = new ArrayList<>(1);
            for (TypeVariable<? extends Class<?>> typeParameter : genericIfc.getTypeParameters()) {
                Type res = map.get(typeParameter);
                while (res instanceof TypeVariable<?>) {
                    res = map.get(res);
                }
                result.add((Class<?>) res);
            }
            return result.toArray(new Class<?>[0]);
        });
    }
}
