package cn.chauncy.utils.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class RpcLagMessageHandler implements MessageHandler<RpcLagMessage> {

    private static final Logger log = LoggerFactory.getLogger(RpcLagMessageHandler.class);

    private static final String RPC_LAG_MESSAGE_STREAM = "RpcLagMessage";
    private static final String RPC_LAG_MESSAGE_GROUP = "RpcLagMessageGroup";

    @Override
    public String streamName() {
        return RPC_LAG_MESSAGE_STREAM;
    }
    @Override
    public String groupName() {
        return RPC_LAG_MESSAGE_GROUP;
    }

    @Override
    public Class<RpcLagMessage> getMessageType() {
        return RpcLagMessage.class;
    }

    @Override
    public RpcLagMessage createMessage(Object... args) {
        Class<?> clazz = (Class<?>) args[0];
        Method method = (Method) args[1];
        Object[] params = (Object[]) args[2];
        return new RpcLagMessage(clazz.getName(), method.getName(),
                method.getParameterTypes(), params, System.currentTimeMillis());
    }

    @Override
    public boolean handle(StreamMessage message) {
        if (!(message instanceof RpcLagMessage)) {
            log.error("message is not match to RpcLagMessage, message: {}", message.getClass().getName());
            return false;
        }

//        RpcLagMessage rpcLagMessage = (RpcLagMessage) message;
//        String serviceName = rpcLagMessage.getServiceName();
//        String methodName = rpcLagMessage.getMethodName();
//        Class<?>[] parameterTypes = rpcLagMessage.getParameterTypes();
//        Object[] params = rpcLagMessage.getArguments();
//        long callTime = rpcLagMessage.getCallTime();
//        if (System.currentTimeMillis() - callTime > TimeUnit.HOURS.toMillis(2)) {
//            return true;
//        }
//
//        try {
//            Map<String, ?> beansOfTypeMap = SpringContextProvider.getApplicationContext().getBeansOfType(Class.forName(serviceName));
//            if (beansOfTypeMap.isEmpty()) {
//                log.error("serviceName={} not found", serviceName);
//                return false;
//            }
//            Object serviceInstance = beansOfTypeMap.values().iterator().next();
//            Class<?> serverClass = serviceInstance.getClass();
//            Method method = serverClass.getMethod(methodName, parameterTypes);
//            method.invoke(serviceInstance, params);
//            return true;
//        } catch (Exception e) {
//            log.error("RpcLagMessage deal error: service={}, method={}, params={}", serviceName, methodName, params, e);
//        }
        return false;
    }
}