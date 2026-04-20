package cn.chauncy;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.component.GlobalIdGenerator;
import cn.chauncy.component.GlobalTimeProvider;
import cn.chauncy.dao.config.*;
import cn.chauncy.disruptor.GameTickProvider;
import cn.chauncy.gameplay.scene.SceneManager;
import cn.chauncy.logic.Managers;
import cn.chauncy.logic.bag.manager.BagManager;
import cn.chauncy.logic.bag.manager.ItemManager;
import cn.chauncy.logic.bag.manager.ResourceManager;
import cn.chauncy.logic.function.FunctionManager;
import cn.chauncy.logic.gm.GMManager;
import cn.chauncy.logic.login.LoginManager;
import cn.chauncy.logic.player.PlayerManager;
import cn.chauncy.logic.task.manager.GoalManager;
import cn.chauncy.logic.task.manager.TaskManager;
import cn.chauncy.manager.GameManager;
import cn.chauncy.net.GameMessageDispatcher;
import cn.chauncy.net.GameMessageRegistry;
import cn.chauncy.services.GameTickEventDispatcherService;
import cn.chauncy.services.HttpFileService;
import cn.chauncy.services.NetService;
import cn.chauncy.services.ScheduleService;
import cn.chauncy.utils.guid.GUIDGenerator;
import cn.chauncy.utils.interceptor.MethodPerf;
import cn.chauncy.utils.interceptor.PerfAnalysisInterceptor;
import cn.chauncy.utils.net.TcpInitializer;
import cn.chauncy.utils.net.config.NettyConfig;
import cn.chauncy.utils.net.handler.MessageDispatcher;
import cn.chauncy.utils.net.proto.MessageRegistry;
import cn.chauncy.utils.net.proto.ProtobufMessage;
import cn.chauncy.utils.thread.ConsoleService;
import cn.chauncy.utils.time.FrameCachedTimeProvider;
import cn.chauncy.utils.time.TimeProvider;
import com.baomidou.mybatisplus.core.MybatisXMLLanguageDriver;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import org.aeonbits.owner.ConfigFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;

import javax.sql.DataSource;

public class GameModule extends AbstractModule {

    public static final GameModule INSTANCE = new GameModule();

    private static final String MAPPER_SCAN_PACKAGE = "cn.chauncy.dao.mapper";

    private GameModule() {}

    @Override
    public void configure() {
        // 强制显式绑定
        binder().requireExplicitBindings();
        // 禁止循环代理，防止代理死循环
        binder().disableCircularProxies();

        bind(GameStarter.class).in(Singleton.class);
        bind(GlobalEventBus.class).in(Singleton.class);
        bind(FrameCachedTimeProvider.class).in(Singleton.class);
        bind(TimeProvider.class).to(GlobalTimeProvider.class).in(Singleton.class);
        bind(GUIDGenerator.class).to(GlobalIdGenerator.class).in(Singleton.class);
        bind(MessageRegistry.class).to(GameMessageRegistry.class).in(Singleton.class);
        bind(new TypeLiteral<MessageDispatcher<ProtobufMessage<?>>>() {}).to(GameMessageDispatcher.class).in(Singleton.class);
        bind(GameTickProvider.class).to(GameManager.class).in(Singleton.class);
        bind(GameTickEventDispatcherService.class).in(Singleton.class);
        bind(TcpInitializer.class).in(Singleton.class);
        bind(NettyConfig.class).toInstance(ConfigFactory.create(NettyConfig.class));

        // Service
        Multibinder<Service> multibinder = Multibinder.newSetBinder(binder(), Service.class);
        multibinder.addBinding().to(NetService.class);
        multibinder.addBinding().to(HttpFileService.class);
        multibinder.addBinding().to(ScheduleService.class);
        multibinder.addBinding().to(ConsoleService.class);
        multibinder.addBinding().to(GameTickEventDispatcherService.class);

        // MyBatisModule
        bind(DefaultObjectWrapperFactory.class).in(Singleton.class);
        bind(DefaultObjectFactory.class).in(Singleton.class);
        install(new MyBatisModule() {
            @Override
            protected void initialize() {
                environmentId("development");
                bindDataSourceProviderType(DataSourceProvider.class);
                bindTransactionFactoryType(JdbcTransactionFactory.class);
                bindDefaultScriptingLanguageType(MybatisXMLLanguageDriver.class);
                bind(MetaObjectHandler.class).to(MyMetaObjectHandler.class).in(Scopes.SINGLETON);

                // 替换应用mybatis-plus的实现
                useSqlSessionFactoryProvider(MybatisSqlSessionFactoryProvider.class);
                useConfigurationProvider(MybatisConfigurationProvider.class);

                mapUnderscoreToCamelCase(true);
                addMapperClasses(MAPPER_SCAN_PACKAGE);
            }
        });

        bind(PlayerManager.class).in(Singleton.class);
        bind(LoginManager.class).in(Singleton.class);
        bind(TaskManager.class).in(Singleton.class);
        bind(GoalManager.class).in(Singleton.class);
        bind(BagManager.class).in(Singleton.class);
        bind(ItemManager.class).in(Singleton.class);
        bind(ResourceManager.class).in(Singleton.class);
        bind(FunctionManager.class).in(Singleton.class);
        bind(GMManager.class).in(Singleton.class);
        bind(SceneManager.class).in(Singleton.class);
        bind(Managers.class).in(Singleton.class);

    }
}
