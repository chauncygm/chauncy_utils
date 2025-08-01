package cn.chauncy;

import lombok.NonNull;

import java.util.concurrent.*;

public class CommonIOExecutor implements Executor {

    public static final CommonIOExecutor MULTI_EXECUTOR = new CommonIOExecutor(false);
    public static final CommonIOExecutor SINGLE_EXECUTOR = new CommonIOExecutor(true);

    private final ThreadPoolExecutor ioExecutor;

    private CommonIOExecutor(boolean singleThread) {
        if (singleThread) {
            this.ioExecutor = new ThreadPoolExecutor(1, 1, 5,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        } else {
            int corePoolSize = Runtime.getRuntime().availableProcessors();
            int maxPoolSize = corePoolSize * 2;
            this.ioExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 5, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(1000),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }
        this.ioExecutor.allowCoreThreadTimeOut(true);
    }

    @Override
    public void execute(@NonNull Runnable command) {
        ioExecutor.execute(command);
    }
}
