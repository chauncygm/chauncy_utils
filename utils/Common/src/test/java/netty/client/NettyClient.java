package netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import netty.server.SimpleHandler;

public class NettyClient {

    private static final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);

    public static void main(String[] args) throws InterruptedException {
        Bootstrap b = new Bootstrap();
        ChannelFuture future = b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new SimpleHandler());
                    }
                }).connect("127.0.0.1", 10001);
        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                System.out.println("连接成功");
            } else {
                System.out.println("连接失败");
            }
        });


    }


}
