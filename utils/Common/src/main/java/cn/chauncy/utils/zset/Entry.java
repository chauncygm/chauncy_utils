package cn.chauncy.utils.zset;

public interface Entry<K, S> {

    K getKey();

    S getValue();
}