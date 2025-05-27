package cn.chauncy;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.manager.LoginManager;
import cn.chauncy.manager.PlayerManager;
import cn.chauncy.net.GameMessageDispatcher;
import cn.chauncy.net.GameMessageRegistry;
import cn.chauncy.services.NetService;
import cn.chauncy.utils.net.handler.MessageDispatcher;
import cn.chauncy.utils.net.proto.MessageRegistry;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.XMLMyBatisModule;

import javax.sql.DataSource;

public class GameModule extends AbstractModule {

    public static final GameModule INSTANCE = new GameModule();

    private static final String MAPPER_SCAN_PACKAGE = "cn.chauncy.dao.mapper";

    private GameModule() {}

    @Override
    public void configure() {
//        binder().requireExplicitBindings();

        binder().bind(GameStarter.class).in(Singleton.class);
        binder().bind(GlobalEventBus.class).in(Singleton.class);
        binder().bind(MessageRegistry.class).to(GameMessageRegistry.class).in(Singleton.class);
        binder().bind(MessageDispatcher.class).to(GameMessageDispatcher.class).in(Singleton.class);

        Multibinder<Service> multibinder = Multibinder.newSetBinder(binder(), Service.class);
        multibinder.addBinding().to(NetService.class);
//        binder().bind(DefaultObjectWrapperFactory.class).in(Singleton.class);
//        binder().bind(DefaultObjectFactory.class).in(Singleton.class);

        // MyBatisModule
        install(new MyBatisModule() {
            @Override
            protected void initialize() {
                environmentId("development");
                bindDataSourceProvider(getProvider(DataSource.class));
                bindTransactionFactory(getProvider(TransactionFactory.class));
                mapUnderscoreToCamelCase(true);

                addMapperClasses(MAPPER_SCAN_PACKAGE);
            }
        });

        binder().bind(PlayerManager.class).in(Singleton.class);
        binder().bind(LoginManager.class).in(Singleton.class);
    }
}
