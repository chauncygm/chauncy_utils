package cn.chauncy.utils.net.handler;

import cn.chauncy.utils.net.proto.IMessage;
import cn.chauncy.utils.net.proto.MessageRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageDispatcher<T extends IMessage> extends SimpleChannelInboundHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    protected MessageRegistry registry;

    public MessageDispatcher(MessageRegistry registry) {
        this.registry = registry;
    }

    public MessageRegistry getRegistry() {
        return registry;
    }

    /**
     * 是否是心跳消息
     * @param msg 消息
     * @return true: 是心跳包
     */
    protected abstract boolean isHeartbeat(T msg);

    /**
     * 处理心跳消息
     * @param ctx 上下文
     */
    protected abstract void heartbeat(ChannelHandlerContext ctx);

    /**
     * 处理消息
     * @param ctx 上下文
     * @param msg 消息
     */
    protected abstract void channelRead0(ChannelHandlerContext ctx, T msg);
}
