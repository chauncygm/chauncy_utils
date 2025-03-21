package netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class SimplerChannelInitializer extends ChannelInitializer<Channel> {

        @Override
        protected void initChannel(Channel ch) {
            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(4096, 0, 2, -2, 0));
            ch.pipeline().addLast(new SimpleHandler());
        }
}
