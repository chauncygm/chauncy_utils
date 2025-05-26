package cn.chauncy.services;

import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class FixedExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(FixedExecutorService.class);

    private final int coreThreadNum;
    private final ExecutorService[] executors;

    public FixedExecutorService(String threadPoolName) {
        coreThreadNum = Runtime.getRuntime().availableProcessors() * 2;
        executors = new ExecutorService[coreThreadNum];
        ThreadFactory threadFactory = new DefaultThreadFactory(threadPoolName);
        for (int i = 0; i < coreThreadNum; i++) {
            executors[i] = Executors.newSingleThreadExecutor(threadFactory);
        }
    }

    public void submit(long index, Runnable runnable) {
        executors[(int)(index % coreThreadNum)].execute(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error("logic thread execute error.", e);
            }
        });
    }

}
