package example;

import com.chauncy.utils.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(UtilsTest.class);

    @BeforeAll
    public static void setup() {
        Utils.setJULLogger();
    }

    @Test
    public void testGetRuntimeInfo() {
        logger.info(Utils.getJVMInfo());
        logger.info(Utils.getInputArguments());
        logger.info(Utils.getSystemProperties());
        logger.info(Utils.getClassPath());
        logger.info(Utils.getNetworkInfo());
        logger.info(Utils.getRuntimeInfo());
        logger.info(Utils.getThreadInfo());
    }
}
