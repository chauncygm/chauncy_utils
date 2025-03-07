package com.chauncy.utils.time;

public interface TimeProvider {

    /** 系统时间 */
    TimeProvider SYSTEM_TIME = System::currentTimeMillis;

    /**
     * 获取时间接口
     *
     * @return 毫秒数
     */
    long getTimeMillis();

}
