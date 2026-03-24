package cn.chauncy.utils.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class PerfAnalysisInterceptor implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(PerfAnalysisInterceptor.class);

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        try {
            long start = System.currentTimeMillis();
            Object result = methodInvocation.proceed();
            long end = System.currentTimeMillis();
            logger.info("method: {}, cost: {}", method.getName(), end - start);
            return result;
        } catch (Exception e) {
            logger.error("method: {}, error: {}", method.getName(), e);
            throw e;
        }
    }

}
