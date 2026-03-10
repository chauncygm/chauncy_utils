package cn.chauncy;

import cn.chauncy.utils.random.RandomUtil;
import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierTest {

    @Test
    public void test() throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(5, () -> {
            System.out.println(Thread.currentThread().getName() + "Over");
        });

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int index = i;
            Thread thread = new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "process " + index);
                try {
                    Thread.sleep(RandomUtil.LOCAL_RANDOM.get().nextInt(5000));
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }, "Thread-" + i);
            thread.start();
            threads[i] = thread;
        }


        Thread.sleep(10000);
        System.out.println("Main thread over");
    }

}
