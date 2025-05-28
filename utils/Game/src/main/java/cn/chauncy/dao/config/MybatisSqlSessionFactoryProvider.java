package cn.chauncy.dao.config;

import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;

@Singleton
public class MybatisSqlSessionFactoryProvider implements Provider<SqlSessionFactory> {

    private SqlSessionFactory sqlSessionFactory;

    @Inject
    public void createNewSqlSessionFactory(final Configuration configuration) {
        // 替换为MybatisSqlSessionFactory
        this.sqlSessionFactory = new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    @Override
    public SqlSessionFactory get() {
        return sqlSessionFactory;
    }
}