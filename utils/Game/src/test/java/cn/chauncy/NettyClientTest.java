package cn.chauncy;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.net.GameMessageDispatcher;
import cn.chauncy.message.ReqLogin;
import cn.chauncy.net.GameMessageRegistry;
import cn.chauncy.utils.net.NettyClient;
import cn.chauncy.utils.net.TcpInitializer;
import cn.chauncy.utils.net.proto.MessageRegistry;

import java.util.Scanner;

import static cn.chauncy.NettyServerTest.HOST;
import static cn.chauncy.NettyServerTest.PORT;

public class NettyClientTest {

    public static void main(String[] args) {
        MessageRegistry registry = new GameMessageRegistry(new GlobalEventBus(), null);
        GameMessageDispatcher dispatcher = new GameMessageDispatcher(registry);
        TcpInitializer channelInitializer = new TcpInitializer(dispatcher);
        NettyClient nettyClient = new NettyClient("client0", channelInitializer);
        nettyClient.connect(HOST, PORT);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String msg = scanner.nextLine();
            if (msg.equals("exit")) {
                nettyClient.close();
                break;
            } else if (msg.equals("test")){
                nettyClient.send(ReqLogin.newBuilder().setUid(1).build());
            } else {
                nettyClient.send(msg);
            }
        }
    }

}
