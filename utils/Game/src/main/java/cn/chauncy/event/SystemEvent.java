package cn.chauncy.event;

/** 系统事件 */
public class SystemEvent {

    record ServerStartEvent() {}

    record ServerStopEvent() {}

    record CrossHourEvent(int hour) {}

    record CrossDayEvent(int epochDay) {}

    record CrossWeekEvent(int weekOfYear) {}

    record CrossMonthEvent(int month) {}

    record CrossYearEvent(int year) {}
}
