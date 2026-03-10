package cn.chauncy.utils.rpc.service;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.bootstrap.builders.ReferenceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClient {

    private static Logger logger = LoggerFactory.getLogger(RpcClient.class);
    public static void main(String[] args) {

        /*
          POST http://localhost:10002/com.chauncy.utils.rpc.service.ILoginService/ping
          Content-Type: application/json
         */

        ApplicationConfig config = new ApplicationConfig("demo-app");
        config.setQosEnable(false);

        ILoginService loginService = (ILoginService) ReferenceBuilder.newBuilder()
                .application(config)
                .interfaceClass(ILoginService.class)
                .url("tri://localhost:50001")
                .build()
                .get();

        String ping = loginService.ping();
        logger.info(ping);
    }
}
