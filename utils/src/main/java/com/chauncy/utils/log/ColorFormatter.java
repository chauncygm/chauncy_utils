package com.chauncy.utils.log;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class ColorFormatter extends SimpleFormatter {

    @Override
    public String format(LogRecord record) {
        String formatText = super.format(record);
        String color = switch (record.getLevel().getName()) {
            case "SEVERE" -> AnsiColor.RED;
            case "WARNING" -> AnsiColor.YELLOW;
            case "INFO" -> AnsiColor.GREEN;
            case "CONFIG" -> AnsiColor.BLUE;
            case "FINE", "FINEST" -> AnsiColor.GREY;
            default -> AnsiColor.RESET;
        };
        return AnsiColor.colorize(formatText, color);
    }

    private static class AnsiColor {
        static final String RESET = "\u001B[0m";
        static final String RED = "\u001B[31m";
        static final String YELLOW = "\u001B[33m";
        static final String BLUE = "\u001B[34m";
        static final String GREEN = "\u001B[32m";
        static final String GREY = "\u001B[90m";

        static String colorize(String text, String color) {
            return color + text + RESET;
        }
    }
}
