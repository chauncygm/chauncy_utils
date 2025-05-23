package netty.server;

import com.chauncy.message.ReqLogin;
import com.chauncy.utils.net.NettyServer;
import com.chauncy.utils.net.SimplerChannelInitializer;
import com.chauncy.utils.net.proto.MessageRegistry;

public class NettyServerTest {

    private static final int PORT = 10001;

    public static void main(String[] args) {
        MessageRegistry registry = new MessageRegistry();
        registry.register(ReqLogin.class);
        SimplerChannelInitializer channelInitializer = new SimplerChannelInitializer(registry, true);
        NettyServer nettyServer = new NettyServer("NettyServer", PORT, channelInitializer);
        nettyServer.bind();
    }

}
