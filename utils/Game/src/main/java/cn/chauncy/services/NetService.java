package cn.chauncy.services;

import cn.chauncy.utils.net.NettyServer;
import cn.chauncy.utils.net.TcpInitializer;
import cn.chauncy.utils.net.config.NettyConfig;
import cn.chauncy.utils.net.handler.MessageDispatcher;
import cn.chauncy.utils.net.proto.ProtobufMessage;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class NetService extends AbstractService {

    private final NettyServer nettyServer;

    @Inject
    public NetService(TcpInitializer channelInitializer, NettyConfig config) {
        this.nettyServer = new NettyServer("nettyServer",  config.port(), channelInitializer);
    }

    @Override
    protected void doStart() {
        ChannelFuture channelFuture = nettyServer.bind();
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                notifyStarted();
            }
        });
    }

    @Override
    protected void doStop() {
        ChannelFuture future = nettyServer.shutdown();
        future.addListener((f) -> {
            if (f.isSuccess()) {
                notifyStopped();
            }
        });

    }
}
