package example;

import com.chauncy.utils.SystemUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(UtilsTest.class);

    @BeforeAll
    public static void setup() {
        SystemUtils.setJULLogger();
    }

    @Test
    public void testGetRuntimeInfo() {
        logger.info(SystemUtils.getJVMInfo());
        logger.info(SystemUtils.getInputArguments());
        logger.info(SystemUtils.getSystemProperties());
        logger.info(SystemUtils.getClassPath());
        logger.info(SystemUtils.getNetworkInfo());
        logger.info(SystemUtils.getRuntimeInfo());
        logger.info(SystemUtils.getThreadInfo());
        logger.info("{}", SystemUtils.getDirectMemoryUsage());
    }
}
