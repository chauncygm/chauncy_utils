package cn.chauncy.dao.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.google.inject.Inject;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.mybatis.guice.configuration.ConfigurationProvider;

public class MybatisConfigurationProvider extends ConfigurationProvider {

    // 元对象自动填充处理器
    private final MetaObjectHandler metaObjectHandler;

    @Inject
    public MybatisConfigurationProvider(Environment environment, MetaObjectHandler metaObjectHandler) {
        super(environment);
        this.metaObjectHandler = metaObjectHandler;
    }

    @Override
    protected Configuration newConfiguration(Environment environment) {
        // 替换为MybatisConfiguration
        MybatisConfiguration mybatisConfiguration = new MybatisConfiguration(environment);
        GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(mybatisConfiguration);
        globalConfig.setMetaObjectHandler(metaObjectHandler);
        return mybatisConfiguration;
    }
}