package cn.chauncy;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.component.GlobalIdGenerator;
import cn.chauncy.dao.config.*;
import cn.chauncy.logic.login.LoginManager;
import cn.chauncy.logic.player.PlayerManager;
import cn.chauncy.net.GameMessageDispatcher;
import cn.chauncy.net.GameMessageRegistry;
import cn.chauncy.services.NetService;
import cn.chauncy.utils.guid.GUIDGenerator;
import cn.chauncy.utils.net.handler.MessageDispatcher;
import cn.chauncy.utils.net.proto.MessageRegistry;
import cn.chauncy.utils.thread.ConsoleService;
import com.baomidou.mybatisplus.extension.ddl.history.IDdlGenerator;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;

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

        binder().bind(GameStarter.class).in(Singleton.class);
        binder().bind(GlobalEventBus.class).in(Singleton.class);
        binder().bind(GUIDGenerator.class).to(GlobalIdGenerator.class).in(Singleton.class);
        binder().bind(MessageRegistry.class).to(GameMessageRegistry.class).in(Singleton.class);
        binder().bind(MessageDispatcher.class).to(GameMessageDispatcher.class).in(Singleton.class);

        Multibinder<Service> multibinder = Multibinder.newSetBinder(binder(), Service.class);
        multibinder.addBinding().to(NetService.class);
        multibinder.addBinding().to(ConsoleService.class);

        // MyBatisModule
        binder().bind(DefaultObjectWrapperFactory.class).in(Singleton.class);
        binder().bind(DefaultObjectFactory.class).in(Singleton.class);
        install(new MyBatisModule() {
            @Override
            protected void initialize() {
                environmentId("development");
                bindDataSourceProviderType(DataSourceProvider.class);
                bindTransactionFactoryType(JdbcTransactionFactory.class);

                // 替换应用mybatis-plus的实现
                useSqlSessionFactoryProvider(MybatisSqlSessionFactoryProvider.class);
                useConfigurationProvider(MybatisConfigurationProvider.class);

                mapUnderscoreToCamelCase(true);
                addMapperClasses(MAPPER_SCAN_PACKAGE);
                addTypeHandlerClass(JacksonTypeHandler.class);
            }
        });

        binder().bind(PlayerManager.class).in(Singleton.class);
        binder().bind(LoginManager.class).in(Singleton.class);
    }
}
