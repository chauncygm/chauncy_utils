package com.chauncy.utils.guid;

import com.chauncy.utils.thread.ThreadUtil;
import com.chauncy.utils.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 雪花算法生成guid
 * 支持2048个节点
 * 毫秒级时间差，可支持约35年
 * 支持10毫秒内生成40960个id
 * <p>
 * 固定为0 | 40位距离开始时间的时间戳 | 11位节点id | 12位序列号
 * 0 | 00000000_00000000_00000000_00000000_00000000 | 000_00000000 | 0000_00000000
 *
 * @author chauncy
 */
public class SnowflakeIdGenerator implements GUIDGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerator.class);

    private static final int TIME_BITS = 40;
    private static final int NODE_ID_BITS = 11;
    private static final int SEQUENCE_BITS = 12;
    private static final int TIME_OFFSET_BITS = NODE_ID_BITS + SEQUENCE_BITS;
    private static final int NODE_OFFSET_BITS = SEQUENCE_BITS;

    /** 默认初始时间 utc 2025-01-01 00:00:00 */
    private static final long INIT_TIME_STAMP = 1735689600000L;
    private static final long MAX_SUPPORT_TIME = 1L << TIME_BITS;

    private static final int MIN_TIME_MS_INTERVAL = 10;
    private static final int MAX_SUPPORT_SEQUENCE_PER_MS = 1 << SEQUENCE_BITS;
    private static final int MAX_SUPPORT_SEQUENCE = MAX_SUPPORT_SEQUENCE_PER_MS * MIN_TIME_MS_INTERVAL;

    private static final long TIME_STAMP_MASK = 0xFF_FFFFFFFFL << TIME_OFFSET_BITS;
    private static final long NODE_ID_MASK = 0x7FFL << NODE_OFFSET_BITS;
    private static final long SEQUENCE_MASK = 0xFFFL;

    private static TimeProvider timeProvider = System::currentTimeMillis;

    /** 节点id，最多支持2047个节点 */
    private final int nodeId;
    /** 初始时间戳，单位毫秒 */
    private final long initTimeStamp;
    /** 当前id段生成距离开始时间的时间戳 */
    private long passTimeStamp;
    /** 序列号，号段支持每秒产生4096个id，我们限制10毫秒内可产生40960个id */
    private final AtomicInteger sequence;

    /** 上次生成的guid */
    private long lastGuid;
    private int sleepCount;

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
        this.sequence = new AtomicInteger(0);
        this.initTimeStamp = initTimeStamp;
    }

    @Override
    public long genGuid() {
        if (passTimeStamp == 0 || parseTimeStamp(lastGuid) >= passTimeStamp + MIN_TIME_MS_INTERVAL) {
            passTimeStamp = timeProvider.getTimeMillis() - initTimeStamp;
        }
        if (passTimeStamp < 0 || passTimeStamp >= MAX_SUPPORT_TIME) {
            throw new IllegalStateException("gen guid error, current time gen guid not support.");
        }

        int seq = sequence.getAndIncrement();
        if (seq < MAX_SUPPORT_SEQUENCE) {
            return genGuid(seq);
        }

        synchronized (this) {
            seq = sequence.getAndIncrement();
            if (seq < MAX_SUPPORT_SEQUENCE) {
                return genGuid(seq);
            }
            long timeMillis = timeProvider.getTimeMillis() - initTimeStamp;
            if (timeMillis >= passTimeStamp + MIN_TIME_MS_INTERVAL) {
                passTimeStamp = timeMillis;
                sequence.set(0);
                return genGuid(0);
            }
            ThreadUtil.sleepQuietly(5);
            sleepCount++;
            return genGuid();
        }
    }

    private long genGuid(long seq) {
        long time = passTimeStamp + (seq >>> SEQUENCE_BITS);
        long guid = time << TIME_OFFSET_BITS
                | (long) nodeId << NODE_OFFSET_BITS
                | seq & SEQUENCE_MASK;
        this.lastGuid = guid;
        return guid;
    }

    private long parseTimeStamp(long seq) {
        return (seq & TIME_STAMP_MASK) >>> TIME_OFFSET_BITS;
    }

    private int parseNodeId(long seq) {
        return (int) ((seq & NODE_ID_MASK) >>> NODE_OFFSET_BITS);
    }

    private long parseSequence(long seq) {
        return seq & SEQUENCE_MASK;
    }

}
