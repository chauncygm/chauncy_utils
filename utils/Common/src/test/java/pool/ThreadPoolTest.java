package pool;

import cn.chauncy.utils.thread.ThreadUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class ThreadPoolTest {

    private static ThreadGroup threadGroup;
    private static ExecutorService syncQueuePool;

    @BeforeAll
    public static void setUp() {
        threadGroup = new ThreadGroup("pool-l");
        syncQueuePool = new ThreadPoolExecutor(0, 3, 0, TimeUnit.MINUTES, new SynchronousQueue<>(),
                new DefaultThreadFactory("pool-l", false, Thread.NORM_PRIORITY, threadGroup));
        syncQueuePool = Executors.newCachedThreadPool(new DefaultThreadFactory("pn", false, Thread.NORM_PRIORITY, threadGroup));
    }

    @Test
    public void test() {

        for (int i = 0; i < 20; i++) {
            syncQueuePool.execute(() -> {
                System.out.println(Thread.currentThread().getName() + "done");
            });
        }

        try {
            Thread.sleep(3000L);
            System.out.println("main done :" + threadGroup.activeCount());

            Thread.sleep(30000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
