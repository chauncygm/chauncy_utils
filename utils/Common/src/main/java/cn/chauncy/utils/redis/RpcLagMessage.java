package cn.chauncy.utils.redis;


import lombok.Data;

@Data
public class RpcLagMessage implements StreamMessage {
    private String serviceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] arguments;
    private long callTime;

    public RpcLagMessage(String serviceName, String methodName, Class<?>[] parameterTypes, Object[] arguments, long callTime) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.arguments = arguments;
        this.callTime = callTime;
    }
}