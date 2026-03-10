package cn.chauncy.services;

import cn.chauncy.utils.thread.ThreadUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class FixedExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(FixedExecutorService.class);

    private final String threadPoolName;
    private final int coreThreadNum;
    private final ExecutorService[] executors;

    public FixedExecutorService(String threadPoolName) {
        this(threadPoolName, Runtime.getRuntime().availableProcessors() * 2);
    }

    public FixedExecutorService(String threadPoolName, int fixedThreadNum) {
        this.threadPoolName = threadPoolName;
        this.coreThreadNum = fixedThreadNum;
        executors = new ExecutorService[coreThreadNum];
        ThreadFactory threadFactory = new DefaultThreadFactory(threadPoolName);
        for (int i = 0; i < coreThreadNum; i++) {
            executors[i] = Executors.newSingleThreadExecutor(threadFactory);
        }
    }

    private String getThreadPoolName() {
        return threadPoolName;
    }

    public void shutdown() {
        for (ExecutorService executor : executors) {
            executor.shutdown();
        }
    }

    public void submit(long index, Runnable runnable) {
        executors[(int)(index % coreThreadNum)].execute(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error("[{}] thread execute error.", ThreadUtil.currentName(), e);
            }
        });
    }

}
