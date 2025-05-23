package pool;

import cn.chauncy.utils.pool.DefaultObjectPool;
import cn.chauncy.utils.pool.Poolable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DefaultObjectPoolTest {



    @Test
    public void test() throws InterruptedException {
        DefaultObjectPool<Item> pool = new DefaultObjectPool<>(10, Item::new);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(() -> {
                List<Item> items = new ArrayList<>();
                for (int j = 1; j <= 10; j++) {
                    Item item = pool.get();
                    item.setIdNum(j, j);
                    items.add(item);
                }
                for (Item item : items) {
                    pool.returnObj(item);
                }
                latch.countDown();
            });
            t.start();
        }

        latch.await();
        System.out.println("pool size: " + pool.size());
        System.out.println("pool createdInstanceCount: " + pool.getCreatedInstanceCount());
        System.out.println("pool outsidePoolSize: " + pool.getOutsidePoolSize());
        System.out.println("pool outsidePool: " + pool.getAllOutsidePool());
        pool.clearOutsidePool();
        System.out.println("pool outsidePool: " + pool.getAllOutsidePool());
    }

    static class Item implements Poolable {
        private int id;
        private int num;

        public void setIdNum(int id, int num) {
            this.num = num;
            this.id = id;
        }

        @Override
        public String toString() {
            return super.toString() + "{" + "id=" + id + ", num=" + num + '}';
        }

        @Override
        public void resetPoolable() {
            this.id = 0;
            this.num = 0;
        }
    }
}
