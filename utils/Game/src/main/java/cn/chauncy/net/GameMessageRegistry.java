package cn.chauncy.net;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.utils.eventbus.Registered;
import cn.chauncy.utils.net.proto.MessageRegistry;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.protobuf.GeneratedMessage;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class GameMessageRegistry extends MessageRegistry {

    private static final Logger logger = LoggerFactory.getLogger(GameMessageRegistry.class);

    private static final String MESSAGE_PACKAGE = "cn.chauncy.message";
    private static final String REGISTRY_PACKAGE = "cn.chauncy.registry";

    private final Injector injector;

    @Inject
    public GameMessageRegistry(GlobalEventBus eventBus, Injector injector) {
        super(eventBus);
        this.injector = injector;
        registerAllProtoMessages();
        registerAllMessageHandler();
    }

    public void registerAllProtoMessages() {
        Reflections reflections = new Reflections(MESSAGE_PACKAGE);
        Set<Class<? extends GeneratedMessage>> protoClasses = reflections.getSubTypesOf(GeneratedMessage.class);

        for (Class<? extends GeneratedMessage> clazz : protoClasses) {
            registerMessage(clazz);
        }
    }

    public void registerAllMessageHandler() {
        if (injector == null) {
            logger.error("registerAllMessageHandler Injector is null");
            return;
        }

        Reflections reflections = new Reflections(REGISTRY_PACKAGE);
        Set<Class<?>> registryClasses = reflections.getTypesAnnotatedWith(Registered.class);

        for (Class<?> clazz : registryClasses) {
            try {
                Method register = clazz.getMethods()[0];
                Class<?> subscriberClass = register.getParameterTypes()[1];
                Object subscriber = injector.getInstance(subscriberClass);
                register.invoke(null, eventBus, subscriber);
            } catch (InvocationTargetException |
                     IllegalAccessException e) {
                logger.error("registerAllMessageHandler failed: {}", clazz.getName(), e);
            }
        }
    }
}
