package cn.chauncy.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameEventExceptionHandler<T> implements ExceptionHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(GameEventExceptionHandler.class);

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        logger.error("Exception in event handler, sequence: {}, event: {}, exception: {}", sequence, event, ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        logger.error("Exception in event handler on start, exception:", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        logger.error("Exception in event handler on shutdown, exception:", ex);
    }
}
