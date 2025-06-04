package cn.chauncy.services;

import cn.chauncy.component.GlobalEventBus;
import cn.chauncy.event.SystemEvent;
import cn.chauncy.utils.time.ShiftableTimeProvider;
import cn.chauncy.utils.time.TimeHelper;
import cn.chauncy.utils.time.TimeProvider;
import cn.chauncy.utils.time.TimeUtils;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class ScheduleService extends AbstractScheduledService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final GlobalEventBus eventBus;
    private final TimeProvider timeProvider;

    private long lastTickTime;

    @Inject
    public ScheduleService(GlobalEventBus eventBus, TimeProvider timeProvider) {
        this.eventBus = eventBus;
        this.timeProvider = timeProvider;
        this.lastTickTime = timeProvider.getTimeMillis();
    }

    @Override
    protected void runOneIteration() {
        long timeMillis = timeProvider.getTimeMillis();
        LocalDateTime time = TimeHelper.SYSTEM.toLocalDateTime(timeMillis);
        if ((timeMillis / 1000) % TimeUtils.SECONDS_PER_MINUTE == 0) {
            logger.info("=== System tick ===");
        }
        if (lastTickTime / TimeUtils.MILLIS_PER_HOUR != timeMillis / TimeUtils.MILLIS_PER_HOUR) {
            onHour(time, timeMillis);
        }
        lastTickTime = timeMillis;
    }

    private void onHour(LocalDateTime time, long timeMillis) {
        if (logger.isDebugEnabled()) {
            logger.debug("System onHour: {}", time);
        }

        eventBus.post(new SystemEvent.CrossHourEvent(time.getHour()));
        if (!TimeHelper.SYSTEM.isSameDay(timeMillis, lastTickTime)) {
            onCrossDay(time, timeMillis);
        }
    }

    private void onCrossDay(LocalDateTime time, long timeMillis) {
        logger.info("System onCrossDay: {}", time);
        eventBus.post(new SystemEvent.CrossDayEvent(TimeHelper.SYSTEM.toEpochDays(timeMillis)));

        if (!TimeHelper.SYSTEM.isSameWeek(timeMillis, lastTickTime)) {
            eventBus.post(new SystemEvent.CrossWeekEvent(time.getMonthValue()));
        }

        int lastTickYear = TimeHelper.SYSTEM.toLocalDateTime(lastTickTime).getYear();
        if (lastTickYear != time.getYear()) {
            eventBus.post(new SystemEvent.CrossYearEvent(lastTickYear));
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected Scheduler scheduler() {
        int initialDelay = 1000 - (int) (timeProvider.getTimeMillis() % 1000);
        return Scheduler.newFixedRateSchedule(initialDelay, 1000, TimeUnit.MILLISECONDS);
    }

}
