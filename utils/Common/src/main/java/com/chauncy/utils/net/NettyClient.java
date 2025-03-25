package com.chauncy.utils.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class NettyClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private final String name;
    private final Bootstrap bootstrap;
    private final EventLoopGroup workEventGroup;
    private SocketAddress remoteAddress;
    private ChannelFuture channelFuture;

    public NettyClient(String name, ChannelInitializer<SocketChannel> initializer) {
        this.name = name;
        bootstrap = new Bootstrap();
        workEventGroup = createWorkEventGroup(name);
        bootstrap.group(workEventGroup);
        bootstrap.channel(getChannelClass());
        bootstrap.handler(initializer);
        bootstrap.option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator());
        if (Epoll.isAvailable()) {
            bootstrap.option(ChannelOption.TCP_FASTOPEN_CONNECT, true)
                    .option(EpollChannelOption.TCP_QUICKACK, true)
                    .option(EpollChannelOption.SO_REUSEPORT, true);
        }
    }

    public void connect(String ip, int port) {
        if (isAlive()) {
            return;
        }
        remoteAddress = new InetSocketAddress(ip, port);
        channelFuture = bootstrap.connect(remoteAddress);
        channelFuture.awaitUninterruptibly(10,  TimeUnit.SECONDS);
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                logger.info("NettyClient[{}] connect success, address: {}", name, remoteAddress);
            } else {
                logger.error("NettyClient[{}] connect error, address: {}, cause", name, remoteAddress, future.cause());
            }
        });
    }

    public void reconnect() {
        if (isAlive()) {
            return;
        }
        channelFuture = bootstrap.connect(remoteAddress);
        channelFuture.awaitUninterruptibly(10,  TimeUnit.SECONDS);
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                logger.info("NettyClient[{}] reconnect success, address: {}", name, remoteAddress.toString());
            } else {
                logger.error("NettyClient[{}] reconnect error, address: {}", name, remoteAddress.toString());
           }
        });
    }

    public void send(Object msg) {
        if (isAlive()) {
            channelFuture.channel().writeAndFlush(msg);
        } else {
            logger.error("NettyClient[{}] is inactive, discard msg: {}", name, msg);
        }
    }

    public void close() {
        if (channelFuture != null) {
            channelFuture.channel().close().addListener(future -> {
                workEventGroup.shutdownGracefully().syncUninterruptibly();
                logger.info("NettyClient[{}] close success", name);
            });
        }
    }

    public boolean isAlive() {
        return channelFuture != null && channelFuture.channel().isActive();
    }

    public SocketAddress getRemoteAddress() {
        return isAlive() ? channelFuture.channel().remoteAddress() : remoteAddress;
    }

    private EventLoopGroup createWorkEventGroup(String groupName) {
        DefaultThreadFactory threadFactory = new DefaultThreadFactory(groupName + "-worker");
        return Epoll.isAvailable() ? new EpollEventLoopGroup(1, threadFactory) : new NioEventLoopGroup(1, threadFactory);
    }

    private Class<? extends SocketChannel> getChannelClass() {
        return Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class;
    }

}
