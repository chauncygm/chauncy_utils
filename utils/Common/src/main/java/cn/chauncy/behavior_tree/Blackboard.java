package cn.chauncy.behavior_tree;

public interface Blackboard {

    void set(String key, Object value);

    <T> T get(String key);

    <T> T getOrDefault(String key, T defaultValue);

    boolean contains(String key);

    void remove(String key);

    void reset();
}
