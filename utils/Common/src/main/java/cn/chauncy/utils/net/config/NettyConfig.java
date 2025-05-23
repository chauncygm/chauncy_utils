package cn.chauncy.utils.net.config;

import org.aeonbits.owner.Config;

@Config.HotReload(2)
@Config.Sources({"classpath:netty.properties", "file:res/config/netty.properties"})
public interface NettyConfig extends Config {

    @Key("port")
    @DefaultValue("8080")
    int port();

    @Key("bossGroupThreads")
    @DefaultValue("1")
    int bossGroupThreads();

    @Key("workGroupThreads")
    @DefaultValue("8")
    int workGroupThreads();

}
