package cn.chauncy.dao.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.util.function.Supplier;

public class MyMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            long now = System.currentTimeMillis();
            Supplier<Long> timeSupplier = () -> now;
            this.strictInsertFill(metaObject, "createTime", timeSupplier, Long.class);
            this.strictInsertFill(metaObject, "updateTime", timeSupplier, Long.class);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            long now = System.currentTimeMillis();
            Supplier<Long> timeSupplier = () -> now;
            this.strictUpdateFill(metaObject, "updateTime", timeSupplier, Long.class);
        }
}
