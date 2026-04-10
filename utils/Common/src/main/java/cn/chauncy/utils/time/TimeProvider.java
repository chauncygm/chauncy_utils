package cn.chauncy.utils.time;

public interface TimeProvider {

    /** 系统时间 */
    TimeProvider SYSTEM_TIME = System::currentTimeMillis;

    /**
     * 获取时间接口
     *
     * @return 毫秒数
     */
    long getTimeMillis();

    /**
     * 手动更新时间，如需要手动更新，可调用此方法
     */
    default void update() {}

}
