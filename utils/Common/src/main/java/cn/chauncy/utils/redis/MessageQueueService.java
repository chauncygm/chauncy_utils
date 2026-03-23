package cn.chauncy.utils.redis;

import com.google.inject.Inject;
import org.redisson.api.*;
import org.redisson.api.stream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MessageQueueService {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueueService.class);

    private static final String CONSUMER_NAME = "consumer";
    private final Map<String, MessageHandler<?>> messageHandlerMap = new HashMap<>();

    @Inject
    private RedissonClient redissonClient;

    public void registerMessageHandler(MessageHandler<?> messageHandler) {
        if (messageHandlerMap.containsKey(messageHandler.getMessageType().getName())) {
            logger.warn("message handler for type: {} already exists", messageHandler.getMessageType().getName());
            return;
        }
        messageHandlerMap.put(messageHandler.getMessageType().getName(), messageHandler);
    }

    public void handMessages(MessageHandler<?> handler) {
        String streamName = handler.streamName();
        String groupName = handler.groupName();

        createConsumerGroupIfNotExist(streamName, groupName);
        RStream<String, Object> rStream = redissonClient.getStream(streamName);
        Map<StreamMessageId, Map<String, Object>> messageMap = popMessages(rStream, groupName, CONSUMER_NAME);
        if (messageMap.isEmpty()) {
            return;
        }

        int ackSize = dealMessages(rStream, groupName, messageMap);
        if (ackSize == messageMap.size()) {
            logger.info("message handler: {} , deal and ack size: {}", handler.getClass().getName(), ackSize);
            rStream.trim(StreamTrimArgs.maxLen(2000).noLimit());
        } else {
            logger.error("message handler: {} deal size: {}, but ack size: {}", handler.getClass().getName(), messageMap.size(), ackSize);
        }
    }

    public void createConsumerGroupIfNotExist(String streamName, String groupName) {
        RStream<Object, Object> rStream = redissonClient.getStream(streamName);
        boolean exist = rStream.listGroups().stream()
                .anyMatch(group -> group.getName().equals(groupName));
        if (exist) {
            return;
        }

        rStream.createGroup(StreamCreateGroupArgs.name(groupName)
                .id(StreamMessageId.ALL)
                .makeStream());
        logger.info("create consumer group: {} for stream: {}", groupName, streamName);
    }

    public void pushMessage(String streamName, StreamMessage message) {
        RStream<String, Object> rStream = redissonClient.getStream(streamName);
        HashMap<String, Object> map = new HashMap<>(4);
        map.put("type", message.getClass().getName());
        map.put("message", message);
        StreamAddArgs<String, Object> addArgs = StreamAddArgs.entries(map);
        addArgs.trim().maxLen(20000).noLimit();
        StreamMessageId messageId = rStream.add(addArgs);
        logger.info("push stream message, streamName: {}, id: {}, message: {}", streamName, messageId,  message);
    }

    public  Map<StreamMessageId, Map<String, Object>> popMessages(RStream<String, Object> rStream, String groupName, String consumerName) {
        Map<StreamMessageId, Map<String, Object>> unAckMessages = rStream.readGroup(groupName, consumerName,
                StreamReadGroupArgs.neverDelivered());
        // 可能存在待处理的消息
        PendingResult pendingInfo = rStream.getPendingInfo(groupName);
        if (pendingInfo != null && pendingInfo.getTotal() > 0) {
            logger.info("Found {} pending messages in group: {}.", pendingInfo.getTotal(), groupName);
            AutoClaimResult<String, Object> claimResult = rStream.autoClaim(groupName, consumerName,
                    2, TimeUnit.MINUTES, StreamMessageId.MIN, (int) pendingInfo.getTotal());
            if (unAckMessages == null) {
                unAckMessages = new HashMap<>();
            }
            unAckMessages.putAll(claimResult.getMessages());
        }
        return unAckMessages == null ? Map.of() : unAckMessages;
    }

    private int dealMessages(RStream<String, Object> rStream, String groupName, Map<StreamMessageId, Map<String, Object>> unAckMessages) {
        int ackSize = 0;
        for (Map.Entry<StreamMessageId, Map<String, Object>> entry : unAckMessages.entrySet()) {
            StreamMessageId messageId = entry.getKey();
            Map<String, Object> valueMap = entry.getValue();
            if (dealMessage(valueMap)) {
                rStream.ack(groupName, messageId);
                ackSize++;
            }
        }
        return ackSize;
    }

    public boolean dealMessage(Map<String, Object> valueMap) {
        Object type = valueMap.get("type");
        Object value = valueMap.get("message");
        if (type == null || value == null) {
            return false;
        }
        MessageHandler<?> messageHandler = messageHandlerMap.get(type.toString());
        if (messageHandler == null || !(value instanceof StreamMessage message)) {
            logger.error("message type or value is error, type: {}, value: {}", type, value);
            return false;
        }

        try {
            return messageHandler.handle(message);
        } catch (Exception e) {
            logger.error("Failed to handle message of type: {}, message: {}", type, value);
        }
        return false;
    }

    public void trimStream(String streamName, int maxLength) {
        RStream<String, Object> rStream = redissonClient.getStream(streamName);
        rStream.trim(StreamTrimArgs.maxLen(maxLength).noLimit());
    }

    public void deleteStream(String streamName, StreamMessageId streamMessageId) {
        RStream<String, Object> rStream = redissonClient.getStream(streamName);
        rStream.remove(streamMessageId);
    }

}
