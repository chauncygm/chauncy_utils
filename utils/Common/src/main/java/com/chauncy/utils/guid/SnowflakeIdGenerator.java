package com.chauncy.utils.guid;

import com.chauncy.utils.thread.ThreadUtil;
import com.chauncy.utils.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;


/**
 * 雪花算法生成guid，与时间强相关，不可回拨时间(可能导致id重复)
 * <p>
 * 支持2048个节点，毫秒级时间差，可运行约35年，支持10毫秒内生成40960个id
 * 测试结果：理论2.5秒可生成1000_0000的id，实际生成1000_0000个id，耗时：3.7秒
 * <p>
 * 固定为0 | 40位距离开始时间的时间戳 | 11位节点id | 12位序列号
 * 0 | 00000000_00000000_00000000_00000000_00000000 | 000_00000000 | 0000_00000000
 *
 * @author chauncy
 */
@ThreadSafe
public class SnowflakeIdGenerator implements GUIDGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerator.class);


    /** 依赖的时间提供者，使用系统时间，保证时钟的连续性 */
    private static final TimeProvider timeProvider = System::currentTimeMillis;

    /** 默认初始时间 utc 2025-01-01 00:00:00 */
    private static final long INIT_TIME_STAMP = 1735689600000L;
    /** 允许序列占用的时间窗口数 */
    private static final int MIN_TIME_INTERVAL = 10;

    /** bit位常量 */
    private static final int TIMESTAMP_BITS = 40;
    private static final int NODE_ID_BITS = 11;
    private static final int SEQUENCE_BITS = 12;
    private static final int TIME_OFFSET_BITS = NODE_ID_BITS + SEQUENCE_BITS;
    private static final int NODE_OFFSET_BITS = SEQUENCE_BITS;

    /** 支持最大值常量 */
    private static final long MAX_SUPPORT_TIME = 1L << TIMESTAMP_BITS;
    private static final int MAX_SUPPORT_SEQUENCE_PER_MS = 1 << SEQUENCE_BITS;
    private static final int MAX_SUPPORT_SEQUENCE = MAX_SUPPORT_SEQUENCE_PER_MS * MIN_TIME_INTERVAL;

    /** 掩码 */
    private static final long TIME_STAMP_MASK = 0xFF_FFFFFFFFL << TIME_OFFSET_BITS;
    private static final long NODE_ID_MASK = 0x7FFL << NODE_OFFSET_BITS;
    private static final long SEQUENCE_MASK = 0xFFFL;

    /** 节点id，最多支持2047个节点 */
    private final int nodeId;
    /** 初始时间戳，单位毫秒， 默认utc时间 2025-01-01 00:00:00 */
    private final long initTimestamp;

    /** 当前id段生成距离开始时间的时间戳 */
    private long timestamp;
    /** 序列号，号段支持每秒产生4096个id，我们扩大时间精度范围，允许10毫秒内可产生40960个id */
    private int sequence;

    /** 上次生成的guid */
    public volatile long lastGuid;
    /** 统计生成id的数量 */
    public volatile long genCount;
    /** 统计sleepCount */
    public volatile int sleepCount;
    /** 统计sleepTime */
    public volatile long sleepTime;

    public SnowflakeIdGenerator(int nodeId) {
        this(nodeId, INIT_TIME_STAMP);
    }

    public SnowflakeIdGenerator(int nodeId, long initTimeStamp) {
        if (nodeId < 0 || nodeId > 2047) {
            throw new IllegalArgumentException("nodeId must be between 0 and 2047");
        }
        if (System.currentTimeMillis() < initTimeStamp) {
            throw new IllegalArgumentException("initTimeStamp must be less than current time");
        }
        this.nodeId = nodeId;
        this.sequence = 0;
        this.initTimestamp = initTimeStamp;
    }

    @Override
    public synchronized long genGuid() {
        // 时间回拨校验，不允许回拨时间
        long lastGenTime = parseTimeStamp(lastGuid);
        long curTimeStamp = getCurTimeStamp();
        if (lastGenTime > initTimestamp + curTimeStamp) {
            throw new IllegalStateException("lastGenTimestamp must be less than current time, lastGenTime: " + lastGenTime);
        }

        // 初始化或更新时间窗口
        if (timestamp == 0 || curTimeStamp >= timestamp + MIN_TIME_INTERVAL) {
            checkoutTimeWindow(curTimeStamp);
        }

        // 获取当前时间窗口的序列id
        if (sequence < MAX_SUPPORT_SEQUENCE) {
            return getGuid();
        }

        // 休眠到下一个时间窗口(如果需要)
        sleepUtilNextTimeWindowIfNeeded(curTimeStamp);
        // 更新时间窗口
        checkoutTimeWindow(getCurTimeStamp());
        return getGuid();
    }

    /** 获取当前相对时间戳 */
    private long getCurTimeStamp() {
        return timeProvider.getTimeMillis() - initTimestamp;
    }

    /**
     * 切换时间窗口
     *
     * @param curTimeStamp 当前相对时间戳
     */
    private void checkoutTimeWindow(long curTimeStamp) {
        if (curTimeStamp < 0 || curTimeStamp >= MAX_SUPPORT_TIME) {
            throw new IllegalStateException("gen guid error, current time gen guid not support.");
        }
        if (curTimeStamp < this.timestamp + MIN_TIME_INTERVAL) {
            throw new IllegalStateException("gen guid error, current time gen guid not support.");
        }
        this.timestamp = curTimeStamp;
        this.sequence = 0;
    }

    /**
     * 如果当前时间窗口的序列已耗尽，则休眠至下一时间窗口
     *
     * @param curTimeStamp 当前相对时间戳
     */
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    private void sleepUtilNextTimeWindowIfNeeded(long curTimeStamp) {
        long sleepTime = timestamp + MIN_TIME_INTERVAL - curTimeStamp;
        if (sleepTime > 0) {
            this.sleepCount++;
            this.sleepTime += sleepTime;
            ThreadUtil.sleepForceQuietly(sleepTime);
        }
    }

    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    private long getGuid() {
        long time = timestamp + (sequence >>> SEQUENCE_BITS);
        long guid = time << TIME_OFFSET_BITS
                | (long) nodeId << NODE_OFFSET_BITS
                | ++sequence & SEQUENCE_MASK;
        this.lastGuid = guid;
        this.genCount++;
        return guid;
    }

    private long parseTimeStamp(long guid) {
        return (guid & TIME_STAMP_MASK) >>> TIME_OFFSET_BITS;
    }

    private int parseNodeId(long guid) {
        return (int) ((guid & NODE_ID_MASK) >>> NODE_OFFSET_BITS);
    }

    private long parseSequence(long guid) {
        return guid & SEQUENCE_MASK;
    }

}
