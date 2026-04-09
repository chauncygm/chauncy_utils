package cn.chauncy.disruptor;

import com.lmax.disruptor.WaitStrategy;

/**
 * 带超时等待的等待策略,需要使 {@link WaitStrategy#waitFor} 支持超时退出等待的机制
 */
public interface TimeoutWaitStrategy extends WaitStrategy {

}
