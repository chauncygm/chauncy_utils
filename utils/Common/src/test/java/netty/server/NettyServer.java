package netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final EventLoopGroup eventExecutors = new NioEventLoopGroup(1, new DefaultThreadFactory("eventExecutors"));
    private final EventLoopGroup childEventExecutors = new NioEventLoopGroup(1, new DefaultThreadFactory("childEventExecutors"));

    private final int port;
    private final String name;
    private ChannelFuture channelFuture;

    public NettyServer(int port, String name) {
        this.port = port;
        this.name = name;
    }

    public static void main(String[] args) {
        try {
            new NettyServer(10001, "NettyServer").startServer();
        } catch (InterruptedException e) {
            logger.error("NettyServer start error", e);
        }
    }

    public void startServer() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        channelFuture = serverBootstrap.group(eventExecutors, childEventExecutors)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())

                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_LINGER, 0)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024))
                .handler(new SimpleAcceptHandler())
                .childHandler(new SimplerChannelInitializer())
                .bind(port);

        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("NettyServer start success, port: {}", port);
            } else {
                logger.error("NettyServer start error, port: {}", port);
            }
        });
    }

    public void shutdown() {
        channelFuture.channel().closeFuture().syncUninterruptibly();
        eventExecutors.shutdownGracefully();
        childEventExecutors.shutdownGracefully();
        logger.info("NettyServer shutdown success, name: {}", name);
    }
}
