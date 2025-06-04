package cn.chauncy.event;

/** 系统事件 */
public class SystemEvent {

    public record ServerStartEvent() {}

    public record ServerStopEvent() {}

    public record CrossHourEvent(int hour) {}

    public record CrossDayEvent(int epochDay) {}

    public record CrossWeekEvent(int weekOfYear) {}

    public record CrossMonthEvent(int month) {}

    public record CrossYearEvent(int year) {}
}
