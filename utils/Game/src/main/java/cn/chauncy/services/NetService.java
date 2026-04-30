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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetService extends AbstractService {

    private static final Logger logger = LoggerFactory.getLogger(NetService.class);

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
        logger.info("NetService stopping...");
        try {
            ChannelFuture future = nettyServer.shutdown();
            // 等待关闭完成，设置超时时间
            future.await(15, java.util.concurrent.TimeUnit.SECONDS);
            if (future.isSuccess()) {
                logger.info("NetService stopped successfully");
            } else {
                logger.warn("NetService shutdown failed: {}", future.cause() != null ? future.cause().getMessage() : "unknown error");
            }
        } catch (InterruptedException e) {
            logger.error("NetService shutdown interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            notifyStopped();
        }
    }
}
