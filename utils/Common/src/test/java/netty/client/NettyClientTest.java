package netty.client;

import com.chauncy.message.ReqLogin;
import com.chauncy.utils.net.NettyClient;
import com.chauncy.utils.net.SimplerChannelInitializer;
import com.chauncy.utils.net.proto.MessageRegistry;

import java.util.Scanner;

public class NettyClientTest {

    private static final String host = "127.0.0.1";
    private static final int port = 10001;

    public static void main(String[] args) {
        MessageRegistry registry = new MessageRegistry();
        registry.register(ReqLogin.class);
        SimplerChannelInitializer channelInitializer = new SimplerChannelInitializer(registry, false);
        NettyClient nettyClient = new NettyClient("client0", channelInitializer);
        nettyClient.connect(host, port);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String msg = scanner.nextLine();
            if (msg.equals("exit")) {
                nettyClient.close();
                break;
            } else {
                nettyClient.send(ReqLogin.newBuilder().setUid(1).build());
            }
        }
    }

}
