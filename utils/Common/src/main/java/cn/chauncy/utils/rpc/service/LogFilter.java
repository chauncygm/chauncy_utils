package cn.chauncy.utils.rpc.service;

import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class LogFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LogFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        long start = System.currentTimeMillis();
        try {
            logger.info("[DUBBO] Call {}.{}, params: {}%n",
                    invocation.getServiceName(),
                    invocation.getMethodName(),
                    Arrays.toString(invocation.getArguments()));
            
            Result result = invoker.invoke(invocation);

            logger.info("[DUBBO] Response {}.{}, cost {}ms, result: {}%n",
                    invocation.getServiceName(),
                    invocation.getMethodName(),
                    System.currentTimeMillis() - start,
                    result.getValue());
            
            return result;
        } catch (RpcException e) {
            System.err.printf("[DUBBO ERROR] %s%n", e.getMessage());
            throw e;
        }
    }
}