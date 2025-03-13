package guid;

import com.chauncy.utils.guid.SnowflakeIdGenerator;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput) // 吞吐量模式
@OutputTimeUnit(TimeUnit.SECONDS) // 输出单位
@State(Scope.Benchmark)
@Threads(10)
@Warmup(iterations = 0)
@Measurement(iterations = 3, time = 5)
@Fork(value = 1)
public class GuidBenchmarkTest {

    private SnowflakeIdGenerator generator;

    @Setup
    public void setup() {
        generator = new SnowflakeIdGenerator(1);
    }

    @Benchmark
    public long testGenGuid() {
        return generator.genGuid();
    }

    @TearDown
    public void tearDown() {
        System.err.println("genCount:" + generator.genCount);
        System.err.println("sleepCount:" + generator.sleepCount);
        System.err.println("sleepTime:" + generator.sleepTime);
    }
}
