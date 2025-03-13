package guid;

import com.chauncy.utils.guid.SnowflakeIdGenerator;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

public class GuidGenerateTest {

    @Test
    public void test() throws Exception {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(0);
        CountDownLatch countDownLatch = new CountDownLatch(5);

        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                long start = System.currentTimeMillis();
                for (int a = 0; a < 1000_0000; a++) {
                    generator.genGuid();
                }
                System.out.println("[" +Thread.currentThread().getName() + "]- use time:" + (System.currentTimeMillis() - start));
                countDownLatch.countDown();
            });
            thread.start();
        }

        countDownLatch.await();
        System.out.println("- gen total time:" + (System.currentTimeMillis() - beginTime));
        System.out.println("- gen guid count:" + generator.genCount);
        System.out.println("- sleep count:" + generator.sleepCount);
        System.out.println("- sleep time:" + generator.sleepTime);
    }
}
