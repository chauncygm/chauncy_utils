package cn.chauncy.utils.time;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 时间工具类
 *
 * @author chauncy
 */
public class TimeUtils {

    private TimeUtils() {}

    //region 时间转换常量
    public static final long NANOS_PER_MILLIS = 1000_000L;
    public static final long NANOS_PER_SECOND = 1000 * NANOS_PER_MILLIS;
    public static final long NANOS_PER_MINUTE = 60 * NANOS_PER_SECOND;
    public static final long NANOS_PER_HOUR = 60 * NANOS_PER_MINUTE;
    public static final long NANOS_PER_DAY = 24 * NANOS_PER_HOUR;
    public static final long NANOS_PER_WEEK = 7 * NANOS_PER_DAY;

    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
    public static final long MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY;

    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE;
    public static final long SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;
    public static final long SECONDS_PER_WEEK = 7 * SECONDS_PER_DAY;

    public static final long MINUTES_PER_HOUR = 60L;
    public static final long MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR;
    public static final long MINUTES_PER_WEEK = 7 * MINUTES_PER_DAY;

    public static final long HOURS_PER_DAY = 24L;
    public static final long HOURS_PER_WEEK = 7 * HOURS_PER_DAY;
    //endregion

    //region 常用的时间格式化
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_FORMAT);
    public static final DateTimeFormatter YMD_HM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
    //endregion

    public static final ZoneOffset ZONE_SYSTEM = ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
    public static final ZoneOffset ZONE_CST = ZoneOffset.of("+8");
    public static final ZoneOffset ZONE_UTC = ZoneOffset.UTC;

    public static final LocalTime START_OF_DAY = LocalTime.MIN;
    public static final LocalTime END_OF_DAY = LocalTime.MAX;

    /** 获取时间单位的字符串缩写 */
    public static String abbreviate(TimeUnit unit) {
        return switch (unit) {
            case NANOSECONDS -> "ns";
            case MICROSECONDS -> "μs";
            case MILLISECONDS -> "ms";
            case SECONDS -> "s";
            case MINUTES -> "min";
            case HOURS -> "h";
            case DAYS -> "d";
        };
    }
}
