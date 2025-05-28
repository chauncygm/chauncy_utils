package cn.chauncy.dao.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.google.inject.Inject;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.mybatis.guice.configuration.ConfigurationProvider;

public class MybatisConfigurationProvider extends ConfigurationProvider {

    @Inject
    public MybatisConfigurationProvider(Environment environment) {
        super(environment);
    }

    @Override
    protected Configuration newConfiguration(Environment environment) {
        // 替换为MybatisConfiguration
        return new MybatisConfiguration(environment);
    }
}