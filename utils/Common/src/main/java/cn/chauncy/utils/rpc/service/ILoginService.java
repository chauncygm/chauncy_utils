package cn.chauncy.utils.rpc.service;

import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public interface ILoginService {

    String ping();

    String hello(String user);
}
