package redis;

import com.chauncy.utils.redis.RedisUtils;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

public class RedisTest {

    @Test
    public void test() {
        Config config = new Config();
        config.setCodec(StringCodec.INSTANCE);
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("root")
                .setDatabase(0)
                .setConnectionPoolSize(4)
                .setConnectionMinimumIdleSize(4)
                .setIdleConnectionTimeout(10000);
        RedisUtils.init(Redisson.create(config));
        System.out.println(RedisUtils.getKey("test"));
    }
}
