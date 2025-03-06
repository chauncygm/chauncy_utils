package com.chauncy.utils.log;

import java.util.logging.*;

public class ColorConsoleHandler extends StreamHandler {

    public ColorConsoleHandler() {
        super();
        setOutputStream(System.out);
        enableWindowsAnsiSupport();
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    // 提取Windows支持方法
    private void enableWindowsAnsiSupport() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            try {
                // 更可靠的方式启用 ANSI
                System.setProperty("jdk.console", "java.base/java.io.Console");
                new ProcessBuilder("cmd", "/c", "chcp 65001").inheritIO().start().waitFor();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Override
    public void close() {
        flush();
    }
}

