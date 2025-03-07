package com.chauncy.utils.time;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import static com.chauncy.utils.time.TimeUtils.*;

/**
 * 时间(特定时区)帮助类
 *
 * @author chauncy
 */
public class TimeHelper {

    /** 系统时区 */
    public static final TimeHelper SYSTEM = new TimeHelper(ZONE_SYSTEM);

    /** UTC时区，通过{@link #of(ZoneOffset)} 获取 */
    private static final TimeHelper UTC = new TimeHelper(ZONE_UTC);
    /** 中国时区，通过{@link #of(ZoneOffset)} 获取 */
    private static final TimeHelper CST = new TimeHelper(ZONE_CST);

    /** 时区 */
    private final ZoneOffset zoneOffset;

    public TimeHelper(ZoneOffset zoneOffset) {
        this.zoneOffset = zoneOffset;
    }

    public static TimeHelper of(ZoneOffset zoneOffset) {
        if (TimeUtils.ZONE_UTC.equals(zoneOffset)) {
            return UTC;
        }
        if (TimeUtils.ZONE_CST.equals(zoneOffset)) {
            return CST;
        }
        if (TimeUtils.ZONE_SYSTEM.equals(zoneOffset)) {
            return SYSTEM;
        }
        return new TimeHelper(zoneOffset);
    }

    /** 获取时区偏移 */
    public ZoneOffset getZoneOffset() {
        return zoneOffset;
    }

    /** 获取时区的秒偏移量 */
    public long getOffsetSeconds() {
        return zoneOffset.getTotalSeconds();
    }

    /** 获取时区的毫秒偏移量 */
    public long getOffsetMillis() {
        return zoneOffset.getTotalSeconds() * 1000L;
    }


    /**
     * 获取指定系统时间的时间戳
     *
     * @param localDateTime 时间
     * @return 对应的时间戳
     */
    public long toEpochMilli(LocalDateTime localDateTime) {
        final long millis = localDateTime.getNano() / TimeUtils.NANOS_PER_MILLIS;
        return localDateTime.toEpochSecond(zoneOffset) * 1000L + millis;
    }

    /**
     * 获取指定系统时间的天数
     *
     * @param epochMilli 时间戳
     * @return 对应的天数
     */
    public int toEpochDays(long epochMilli) {
        long localSec = epochMilli / 1000 + zoneOffset.getTotalSeconds();
        return Math.toIntExact(localSec / SECONDS_PER_DAY);
    }

    /**
     * 将时间戳转换为本地时间
     *
     * @param epochMilli 时间戳
     * @return 本地时间
     */
    public LocalDateTime toLocalDataTime(long epochMilli) {
        long second = epochMilli / 1000;
        int nanosOfSecond = (int) (epochMilli  % 1000 * TimeUtils.NANOS_PER_MILLIS);
        return LocalDateTime.ofEpochSecond(second, nanosOfSecond, zoneOffset);
    }

    /**
     * 将时间戳转换为本地时间(仅精确到秒)
     *
     * @param epochMilli 时间戳
     * @return 本地时间
     */
    public LocalDateTime toLocalDataTimeSec(long epochMilli) {
        return LocalDateTime.ofEpochSecond(epochMilli / 1000, 0, zoneOffset);
    }

    /**
     * 获取当天开始的时间戳
     */
    public long getBeginOfDay() {
        LocalDateTime now = LocalDateTime.now();
        return toEpochMilli(now.with(START_OF_DAY));
    }

    /**
     * 获取指定时间当天开始的时间戳
     *
     * @param epochMilli 时间戳
     * @return 当天开始的时间戳
     */
    public long getBeginOfDay(long epochMilli) {
        LocalDateTime localDataTime = toLocalDataTimeSec(epochMilli);
        return toEpochMilli(localDataTime.with(START_OF_DAY));
    }

    /**
     * 获取指定时间当天结束的时间戳
     *
     * @param epochMilli 时间戳
     * @return 当天结束的时间戳
     */
    public long getEndOfDay(long epochMilli) {
        LocalDateTime localDataTime = toLocalDataTimeSec(epochMilli);
        return toEpochMilli(localDataTime.with(END_OF_DAY));
    }

    /**
     * 获取明天的开始时间戳
     *
     * @return 明天的开始时间戳
     */
    public long getNextBeginOfDay() {
        LocalDateTime now = LocalDateTime.now();
        return toEpochMilli(now.plusDays(1).with(START_OF_DAY));
    }

    /**
     * 获取指定时间明天开始的时间戳
     *
     * @param epochMilli 时间戳
     * @return 明天开始的时间戳
     */
    public long getNextBeginOfDay(long epochMilli) {
        LocalDateTime localDateTime = toLocalDataTimeSec(epochMilli);
        return toEpochMilli(localDateTime.plusDays(1).with(START_OF_DAY));
    }

    /**
     * 获取指定时间当天指定小时数的时间戳
     *
     * @param epochMilli 时间戳
     * @param hour 小时数
     * @return 当天指定小时数的时间戳
     */
    public long getTimeHourOfDay(long epochMilli, int hour) {
        LocalDateTime localDataTime = toLocalDataTimeSec(epochMilli);
        LocalDateTime adjustTime = localDataTime.with(LocalTime.of(hour, 0));
        return toEpochMilli(adjustTime);
    }

    /**
     * 获取指定时间当前周开始的时间戳
     *
     * @param epochMilli 时间戳
     * @return 当前周开始的时间戳
     */
    public long getBeginOfWeek(long epochMilli) {
        LocalDateTime localDataTime = toLocalDataTimeSec(epochMilli);
        LocalDateTime adjustTime = localDataTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        return toEpochMilli(adjustTime);
    }

    /**
     * 获取指定时间下一周开始的时间戳
     *
     * @param epochMilli 时间戳
     * @return 下一周开始的时间戳
     */
    public long getNextBeginOfWeek(long epochMilli) {
        LocalDateTime localDataTime = toLocalDataTimeSec(epochMilli);
        LocalDateTime adjustTime = localDataTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        return toEpochMilli(adjustTime);
    }

    /**
     * 获取指定时间本月开始的时间戳
     *
     * @param epochMilli 时间戳
     * @return 本月开始的时间戳
     */
    public long getBeginOfMonth(long epochMilli) {
        LocalDateTime localDataTime = toLocalDataTimeSec(epochMilli);
        LocalDateTime adjustTime = localDataTime.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        return toEpochMilli(adjustTime);
    }

    /**
     * 获取指定时间下月开始的时间戳
     *
     * @param epochMilli 时间戳
     * @return 下月开始的时间戳
     */
    public long getNextBeginOfMonth(long epochMilli) {
        LocalDateTime localDataTime = toLocalDataTimeSec(epochMilli);
        LocalDateTime adjustTime = localDataTime.with(TemporalAdjusters.firstDayOfNextMonth()).with(LocalTime.MIN);
        return toEpochMilli(adjustTime);
    }

    /**
     * 获取当月天数
     *
     * @param localDateTime 时间
     * @return 当月的天数
     */
    public int getLengthOfMonth(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate().lengthOfMonth();
    }

    /**
     * 判断两个时间戳是否是同一天
     */
    public boolean isSameDay(long time1, long time2) {
        return toEpochDays(time1) == toEpochDays(time2);
    }

    /**
     * 判断两个时间戳是否是同一周
     */
    public boolean isSameWeek(long time1, long time2) {
        return getBeginOfWeek(time1) == getBeginOfWeek(time2);
    }

    /**
     * 计算两个时间戳相差的天数
     */
    public int differDays(long time1, long time2) {
        return Math.abs(toEpochDays(time1) - toEpochDays(time2));
    }

    /**
     * 计算两个时间戳相差的周数
     */
    public int differWeeks(long time1, long time2) {
        long differMills = getBeginOfWeek(time1) - getBeginOfWeek(time2);
        return (int) Math.abs(differMills / MILLIS_PER_WEEK);
    }

    //region 时间格式化
    /**
     * 将时间字符串转换为时间戳
     */
    public long parseTime(String time) {
        return parseTime(time, DEFAULT_FORMATTER);
    }

    /**
     * 将时间字符串转换为指定格式的时间戳
     */
    public long parseTime(String time, DateTimeFormatter formatter) {
        LocalDateTime localDataTime = LocalDateTime.parse(time, formatter);
        return toEpochMilli(localDataTime);
    }

    /**
     * 将时间戳转换为时间字符串
     */
    public String formatTime(long epochMilli) {
        return formatTime(epochMilli, DEFAULT_FORMATTER);
    }

    /**
     * 将时间戳转换为指定格式时间字符串
     */
    public String formatTime(long epochMilli, DateTimeFormatter formatter) {
        LocalDateTime localDataTime = toLocalDataTime(epochMilli);
        return formatTime(localDataTime, formatter);
    }

    /**
     * 将时间转换为指定格式时间字符串
     */
    public String formatTime(LocalDateTime localDateTime, DateTimeFormatter formatter) {
        return localDateTime.format(formatter);
    }

    /**
     * 将时间转换为在指定时区下的时间字符串
     */
    public String formatFromTimeAtZone(LocalDateTime localDateTime, ZoneOffset otherZoneOffset) {
        long epochMilli = toEpochMilli(localDateTime);
        return formatFromTimeAtZone(epochMilli, otherZoneOffset);
    }

    /**
     * 将时间转换为在指定时区下的时间字符串
     */
    public String formatFromTimeAtZone(long epochMilli, ZoneOffset otherZoneOffset) {
        return Instant.ofEpochMilli(epochMilli).atOffset(otherZoneOffset).format(DEFAULT_FORMATTER);
    }
    //endregion

}
