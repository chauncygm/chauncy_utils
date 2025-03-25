package netty.client;

import com.chauncy.utils.net.NettyClient;
import com.chauncy.utils.net.SimplerChannelInitializer;

import java.util.Scanner;

public class NettyClientTest {

    private static final String host = "127.0.0.1";
    private static final int port = 10001;

    public static void main(String[] args) {
        SimplerChannelInitializer channelInitializer = new SimplerChannelInitializer();
        NettyClient nettyClient = new NettyClient("client0", channelInitializer);
        nettyClient.connect(host, port);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String msg = scanner.nextLine();
            if (msg.equals("exit")) {
                nettyClient.close();
                break;
            } else {
                nettyClient.send(msg);
            }
        }
    }

}
