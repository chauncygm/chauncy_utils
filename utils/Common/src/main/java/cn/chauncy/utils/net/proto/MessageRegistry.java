package cn.chauncy.utils.net.proto;

import cn.chauncy.utils.eventbus.EventBus;
import cn.chauncy.utils.eventbus.GenericEvent;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.zip.CRC32;

public class MessageRegistry {

    private static final Logger logger = LoggerFactory.getLogger(MessageRegistry.class);

    private final Int2ObjectOpenHashMap<Class<? extends Message>> messageMap = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Parser<? extends Message>> parserMap = new Int2ObjectOpenHashMap<>();

    protected final EventBus eventBus;

    public MessageRegistry(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void registerMessage(Class<? extends Message> clazz) {
        int protoEnum = getProtoEnum(clazz);
        if (messageMap.containsKey(protoEnum)) {
            Class<? extends Message> lastClazz = messageMap.get(protoEnum);
            throw new IllegalArgumentException("protoEnum repeated, last:" + lastClazz +  ", current: " + clazz);
        }

        Parser<? extends Message> parser = findMessageParser(clazz);
        if (parser == null) {
            throw new IllegalArgumentException("clazz not found parser, clazz: " + clazz.getSimpleName());
        }
        parserMap.put(protoEnum, parser);
        messageMap.put(protoEnum, clazz);
    }

    public int getProtoEnum(Class<? extends Message> clazz) {
        CRC32 crc32 = new CRC32();
        crc32.update(clazz.getSimpleName().getBytes());
        return (int) crc32.getValue();
    }

    public Parser<? extends Message> getParser(int protoEnum) {
        return parserMap.get(protoEnum);
    }

    public Class<? extends Message> getMessageClass(int protoEnum) {
        return messageMap.get(protoEnum);
    }

    public void postMessageEvent(GenericEvent event) {
        long startTime = System.currentTimeMillis();
        try {
            eventBus.post(event);
        } finally {
            long endTime = System.currentTimeMillis();
            if (endTime - startTime > 100) {
                logger.warn("postMessageEvent cost {}ms, event: {}", endTime - startTime, event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Message> Parser<T> findMessageParser(Class<T> messageClass) {
        try {
            Method method = messageClass.getDeclaredMethod("parser");
            method.setAccessible(true);
            return (Parser<T>) method.invoke(null);
        } catch (ReflectiveOperationException ex) {
            // ignore
        }
        try {
            Field field = messageClass.getDeclaredField("PARSER");
            field.setAccessible(true);
            return (Parser<T>) field.get(null);
        } catch (ReflectiveOperationException ex) {
            // ignore
        }
        return null;
    }

}
