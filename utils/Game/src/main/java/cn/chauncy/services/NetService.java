package cn.chauncy.services;

import cn.chauncy.utils.net.NettyServer;
import cn.chauncy.utils.net.TcpInitializer;
import cn.chauncy.utils.net.handler.MessageDispatcher;
import cn.chauncy.utils.net.proto.MessageRegistry;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class NetService extends AbstractService {

    private final NettyServer nettyServer;

    @Inject
    @SuppressWarnings("rawtypes")
    public NetService(MessageRegistry registry, MessageDispatcher dispatcher) {
        ChannelInitializer<SocketChannel> channelInitializer  = new TcpInitializer(dispatcher);
        this.nettyServer = new NettyServer("nettyServer",  10001, channelInitializer);
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
