package cn.chauncy.utils.redis;

public interface MessageHandler<T extends StreamMessage> {

    /**
     * 获取消息对应的 streamName
     */
    String streamName();

    /**
     * 消费组名
     */
    String groupName();

    /**
     * 消息类型
     */
    Class<T> getMessageType();

    /**
     * 创建消息
     */
    T createMessage(Object... args);

    /**
     * 处理消息
     * @param message 消息
     * return 是否处理成功，true表示处理成功，可以ack，false表示处理失败
     */
    boolean handle(StreamMessage message);

}
