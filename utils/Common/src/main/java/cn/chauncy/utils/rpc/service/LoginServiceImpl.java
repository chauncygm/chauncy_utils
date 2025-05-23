package cn.chauncy.utils.rpc.service;

public class LoginServiceImpl implements ILoginService {

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    public String hello(String user) {
        return "hello, " + user + ".";
    }


}
