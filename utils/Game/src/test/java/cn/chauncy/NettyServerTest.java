package cn.chauncy;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.net.GameMessageDispatcher;
import cn.chauncy.net.GameMessageRegistry;
import cn.chauncy.utils.net.NettyServer;
import cn.chauncy.utils.net.TcpInitializer;
import cn.chauncy.utils.net.proto.MessageRegistry;

public class NettyServerTest {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 10001;

    public static void main(String[] args) {
        MessageRegistry registry = new GameMessageRegistry(new GlobalEventBus(), null);
        GameMessageDispatcher dispatcher = new GameMessageDispatcher(registry);

        TcpInitializer channelInitializer = new TcpInitializer(dispatcher);
        NettyServer nettyServer = new NettyServer("NettyServer", PORT, channelInitializer);
        nettyServer.bind();
    }

}
