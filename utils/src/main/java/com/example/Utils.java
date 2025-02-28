package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.LogManager;

public class Utils {

    public static void setJULLogger() {
        try (InputStream is = Utils.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            //ignore
        }
    }

    public static void printLoggerSetting() {
        Handler[] handlers = java.util.logging.Logger.getLogger("").getHandlers();
        for (Handler handler : handlers) {
            System.out.println("Handler: " + handler.getClass().getName() + ", Level: " + handler.getLevel());
        }
    }
}
