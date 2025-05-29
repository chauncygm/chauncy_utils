package cn.chauncy.component;

import cn.chauncy.utils.guid.GUIDGenerator;
import cn.chauncy.utils.guid.SnowflakeIdGenerator;


public class GlobalIdGenerator implements GUIDGenerator {

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1);

    @Override
    public long genGuid() {
        return idGenerator.genGuid();
    }
}
