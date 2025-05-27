package cn.chauncy.dao.config;

import org.aeonbits.owner.Config;

@Config.HotReload(2)
@Config.Sources(value = "application.properties")
public interface DataBaseConfig extends Config {

    @Key("db.url")
    String dbUrl();

    @Key("db.username")
    String dbUsername();

    @Key("db.password")
    String dbPassword();

    @Key("db.driver")
    String dbDriver();

    // HikariCP 配置
    @Key("hikari.maximumPoolSize")
    @DefaultValue("10")
    int hikariMaximumPoolSize();

    @Key("hikari.minimumIdle")
    @DefaultValue("2")
    int hikariMinimumIdle();

    @Key("hikari.idleTimeout")
    @DefaultValue("30000")
    long hikariIdleTimeout();

    @Key("hikari.maxLifetime")
    @DefaultValue("1800000")
    long hikariMaxLifetime();

    @Key("hikari.connectionTestQuery")
    @DefaultValue("SELECT 1")
    String hikariConnectionTestQuery();

}
