package com.example;

import com.chauncy.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.util.Date;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws InterruptedException {
        SystemUtils.setJULLogger();
        logger.info(SystemUtils.getInputArguments());
        logger.info(SystemUtils.getRuntimeInfo());

        Cleaner cleaner = Cleaner.create();
        cleaner.register(new Object(), () -> {
            System.out.printf("cleaner is called ： %s", new Date());
        });
        Thread.sleep(150_000L);
    }
}
