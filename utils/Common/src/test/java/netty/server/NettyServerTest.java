package netty.server;

import com.chauncy.utils.net.NettyServer;
import com.chauncy.utils.net.SimplerChannelInitializer;
import io.netty.channel.epoll.Epoll;

public class NettyServerTest {

    private static final int PORT = 10001;

    public static void main(String[] args) {
        NettyServer nettyServer = new NettyServer("NettyServer");
        SimplerChannelInitializer channelInitializer = new SimplerChannelInitializer();
        nettyServer.start(channelInitializer, PORT);
    }

}
