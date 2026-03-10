package cn.chauncy.utils.rpc.service;

import cn.chauncy.utils.thread.ThreadUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.SystemPropertyConfigUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.bootstrap.builders.RegistryBuilder;
import org.apache.dubbo.config.bootstrap.builders.ServiceBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.dubbo.common.constants.CommonConstants.DubboProperty.DUBBO_PREFER_JSON_FRAMEWORK_NAME;

public class RpcManager {

    private DubboBootstrap dubboBootstrap;
    private final RpcConfig config;
    private final List<ServiceConfig<?>> services = new ArrayList<>();

    public RpcManager(RpcConfig config) {
        if (config.protocols.isEmpty()) {
            ProtocolConfig protocolConfig = new ProtocolConfig(CommonConstants.TRIPLE, 50001);
            protocolConfig.setThreadpool("fixed");
            protocolConfig.setThreads(10);
            config.protocols.add(protocolConfig);
        }
        this.config = config;
    }

    public <T> void registerService(Class<T> interfaceClass, T serviceImpl) {
        @SuppressWarnings("unchecked")
        ServiceConfig<T> service = (ServiceConfig<T>) ServiceBuilder.newBuilder()
                .interfaceClass(interfaceClass)
                .ref(serviceImpl)
                .version(config.version)
                .build();
        services.add(service);
    }

    public void start() throws ClassNotFoundException {
        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap.application(config.appName);

        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setExecutorManagementMode("ISOLATION");
        applicationConfig.setQosEnable(false);

        for (ProtocolConfig protocol : config.protocols) {
            protocol.setOptimizer(config.optimizerImplClass);
        }
        bootstrap.protocols(Collections.unmodifiableList(config.protocols));

        RegistryConfig registryConfig = RegistryBuilder.newBuilder()
                .address(config.registryAddress)
                .version(config.version)
                .build();

        bootstrap.registry(registryConfig);
        bootstrap.services(Collections.unmodifiableList(services));
        bootstrap.asyncStart();
    }

    public boolean isRunning() {
        return dubboBootstrap != null && dubboBootstrap.isRunning();
    }

    public void stop() {
        if (dubboBootstrap != null) {
            dubboBootstrap.stop();
        }
    }

    public static class RpcConfig {

        private String appName= "DEFAULT_APP";
        private String registryAddress = "zookeeper://127.0.0.1:2181";
        private String version = "1.0.0";
        private String optimizerImplClass;
        private List<ProtocolConfig> protocols = new ArrayList<>();

        public String getAppName() {
            return appName;
        }

        public RpcConfig setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public String getRegistryAddress() {
            return registryAddress;
        }

        public RpcConfig setRegistryAddress(String registryAddress) {
            this.registryAddress = registryAddress;
            return this;
        }

        public String getVersion() {
            return version;
        }

        public RpcConfig setVersion(String version) {
            this.version = version;
            return this;
        }

        public String getOptimizerImplClass() {
            return optimizerImplClass;
        }

        public RpcConfig setOptimizerImplClass(String optimizerImplClass) {
            this.optimizerImplClass = optimizerImplClass;
            return this;
        }

        public List<ProtocolConfig> getProtocols() {
            return protocols;
        }

        public RpcConfig setProtocols(List<ProtocolConfig> protocols) {
            this.protocols = protocols;
            return this;
        }

    }

    public static void main(String[] args) throws ClassNotFoundException {
        RpcConfig config = new RpcConfig();
        config.setAppName("demo-app")
                .setRegistryAddress("zookeeper://172.21.240.55:2181")
                .setVersion("1.0.0")
                .setOptimizerImplClass("cn.chauncy.utils.rpc.service.SerializationOptimizerImpl")
                .setProtocols(List.of(new ProtocolConfig(CommonConstants.TRIPLE, 50001)));
        RpcManager rpcManager = new RpcManager(config);
        rpcManager.registerService(ILoginService.class, new LoginServiceImpl());
        rpcManager.start();


        ThreadUtil.sleepForceQuietly(Long.MAX_VALUE);
    }

    static class DubboMigrationConfig{


        public ApplicationConfig applicationCOnfig() {
            return new ApplicationConfig();
        }
    }

}
