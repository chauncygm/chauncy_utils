package netty.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAcceptHandler implements ChannelHandler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAcceptHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.info("ServerHandler added: {}", ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger.info("ServerHandler removed: {}", ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.info("ServerHandler exceptionCaught: {}", ctx);
    }
}
