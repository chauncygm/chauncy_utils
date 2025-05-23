package com.chauncy.utils.net;


import com.chauncy.utils.net.handler.SimpleHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ChannelInitializer<SocketChannel> channelInitializer;
    private int port;

    private volatile boolean running = false;
    private ChannelFuture channelFuture;

    public NettyServer(String name, int port, ChannelInitializer<SocketChannel> channelInitializer) {
        int processors = Runtime.getRuntime().availableProcessors();
        int bossThreadCount = Math.max(processors / 2, 1);
        this.bossGroup = createEventLoopGroup(name + "-boss", bossThreadCount);
        this.workerGroup = createEventLoopGroup(name + "-worker", processors * 2);
        this.channelInitializer = channelInitializer;
        this.port = port;
    }


    public ChannelFuture bind() {
        if (running) {
            return channelFuture;
        }

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup);
        serverBootstrap.channel(getSocketChannelClass());
        serverBootstrap.localAddress(port);
        serverBootstrap.option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator());
        if (Epoll.isAvailable()) {
            serverBootstrap.option(ChannelOption.TCP_FASTOPEN, 3);
        }

        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.AUTO_READ, true)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024));
//         更精细地控制连接关闭的情况
//                .childOption(ChannelOption.SO_LINGER, 3)
//                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, true);

        serverBootstrap.handler(new SimpleHandler(true));
        serverBootstrap.childHandler(channelInitializer);
        channelFuture = serverBootstrap.bind();
        channelFuture.channel().closeFuture().addListener(future -> {
            logger.info("NettyServer[{}] stop", this.port);
            shutdown();
        });
        return channelFuture;

    }

    public ChannelFuture shutdown() {
        ChannelFuture closeFuture = channelFuture.channel().close();
        closeFuture.addListener(future -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            running = false;
            logger.info("NettyServer[{}] stop success", this.port);
        });
        return closeFuture;
    }

    @SuppressWarnings("unused")
    public boolean isRunning() {
        return running;
    }


    private EventLoopGroup createEventLoopGroup(String groupName, int threadCount) {
        DefaultThreadFactory threadFactory = new DefaultThreadFactory(groupName);
        return  Epoll.isAvailable() ? new EpollEventLoopGroup(threadCount, threadFactory) : new NioEventLoopGroup(threadCount, threadFactory);
    }

    private Class<? extends ServerSocketChannel> getSocketChannelClass() {
        return Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }
}
