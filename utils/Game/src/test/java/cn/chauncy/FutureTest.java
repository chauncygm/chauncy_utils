package cn.chauncy;


import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class FutureTest {


    @Test
    public void test() throws ExecutionException, InterruptedException {
        Executor executor = Executors.newSingleThreadExecutor();
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "data";
        }, executor);

        CompletableFuture<Void> future2 = future1.thenAccept(new SimpleConsumer("A1"));
        future1.thenAccept(new SimpleConsumer("A2"));
        future1.thenAccept(new SimpleConsumer("A3"));

        CompletableFuture<Void> future = future2.thenAccept(new SimpleConsumer("B1"));
        future2.thenAccept(new SimpleConsumer("B2"));
        CompletableFuture<Void> future3 = future2.thenAccept(new SimpleConsumer("B3"));

//        future1.orTimeout(5, TimeUnit.SECONDS)
//                .exceptionally(throwable -> {
//                    if (throwable instanceof java.util.concurrent.TimeoutException) {
//                        System.out.println("Timeout");
//                    }
//                    return null;
//                });


        future3.thenAccept(new SimpleConsumer("C1"));
        future3.thenAccept(new SimpleConsumer("C2"));
        future3.thenAccept(new SimpleConsumer("C3"));
        System.out.println("Done");
        try {
            Thread.sleep(1000000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(future.get());
    }

    static class SimpleConsumer implements Consumer<Object> {

        private final String name;

        public SimpleConsumer(String name) {
            this.name = name;
        }

        @Override
        public void accept(Object s) {
            System.out.println(Thread.currentThread().getName() + " --> " + name + " : " + s);
        }
    }

}
