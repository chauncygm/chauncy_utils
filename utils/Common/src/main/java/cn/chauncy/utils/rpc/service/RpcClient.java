package cn.chauncy.utils.rpc.service;

import org.apache.dubbo.config.bootstrap.builders.ReferenceBuilder;

public class RpcClient {

    public static void main(String[] args) {
        /*
          POST http://localhost:10002/com.chauncy.utils.rpc.service.ILoginService/ping
          Content-Type: application/json
         */
        ILoginService loginService = (ILoginService) ReferenceBuilder.newBuilder()
                .interfaceClass(ILoginService.class)
                .url("tri://localhost:10002")
                .build()
                .get();

        String ping = loginService.ping();
        System.out.println(ping);
    }
}
