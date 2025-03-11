package com.example;

import com.chauncy.utils.Utils;
import com.chauncy.utils.reload.InstHelper;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws InterruptedException {
        Utils.setJULLogger();
        logger.info(Utils.getInputArguments());
        logger.info(Utils.getRuntimeInfo());

        Cleaner cleaner = Cleaner.create();
        cleaner.register(new Object(), () -> {
            System.out.printf("cleaner is called ： %s", new Date());
        });
        Thread.sleep(150_000L);
    }
}
