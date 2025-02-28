package com.formatter;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomLogFormatter extends Formatter {

    private static final String FORMAT = "[%1$tF %1$tT] [%2$7s] [%3$s]-%4$s.%5$s : %6$s%n";

    @Override
    public String format(LogRecord record) {
        return String.format(FORMAT,
                new Date(record.getMillis()),        // 时间
                record.getLevel(),                   // 日志级别
                Thread.currentThread().getName(),    // 线程名
                record.getSourceClassName(),         // 类路径
                record.getSourceMethodName(),        // 方法名
//                record.getSourceLineNumber(),        // 行号（需编译时包含调试信息）
//                record.getLoggerName(),              // Logger 名称（可选）
                record.getMessage()                  // 消息内容
        );
    }
}