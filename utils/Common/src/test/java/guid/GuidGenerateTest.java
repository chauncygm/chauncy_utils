package guid;

import com.chauncy.utils.guid.SnowflakeIdGenerator;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class GuidGenerateTest {

    @Test
    public void test() throws Exception {
        Set<Long> ids = Sets.newConcurrentHashSet();
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(0);
        CountDownLatch countDownLatch = new CountDownLatch(5);

        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                long start = System.currentTimeMillis();
                for (int a = 0; a < 200_0000; a++) {
                    ids.add(generator.genGuid());
                }
                System.out.println("[" +Thread.currentThread().getName() + "]- use time:" + (System.currentTimeMillis() - start));
                countDownLatch.countDown();
            });
            thread.start();
        }

        countDownLatch.await();
        System.out.println("- gen uni guid size:" + ids.size());
        System.out.println("- gen total time:" + (System.currentTimeMillis() - beginTime));
        System.out.println("- gen guid count:" + generator.genCount);
        System.out.println("- sleep count:" + generator.sleepCount);
        System.out.println("- sleep time:" + generator.sleepTime);
    }
}
