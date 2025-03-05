package com.example;

import com.chauncy.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.util.Date;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws InterruptedException {

        Utils.setJULLogger();

        logger.info(Utils.getJVMInfo());
        logger.info(Utils.getRuntimeInfo());
        logger.info(Utils.getNetworkInfo());


        Cleaner cleaner = Cleaner.create();
        cleaner.register(new Object(), () -> {
            System.out.printf("cleaner is called ： %s", new Date());
        });

        Thread.sleep(100_000L);
    }
}
