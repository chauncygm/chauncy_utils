package cn.chauncy.dao.config;

import com.google.inject.Provider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.aeonbits.owner.ConfigFactory;

import javax.sql.DataSource;

public class DataSourceProvider implements Provider<DataSource> {

    @Override
    public DataSource get() {
        DataBaseConfig config = ConfigFactory.create(DataBaseConfig.class);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.dbUrl());
        hikariConfig.setUsername(config.dbUsername());
        hikariConfig.setPassword(config.dbPassword());
        hikariConfig.setDriverClassName(config.dbDriver());
        hikariConfig.setMaximumPoolSize(config.hikariMaximumPoolSize());
        hikariConfig.setMinimumIdle(config.hikariMinimumIdle());
        hikariConfig.setIdleTimeout(config.hikariIdleTimeout());
        hikariConfig.setMaxLifetime(config.hikariMaxLifetime());
        return new HikariDataSource(hikariConfig);
    }
}
