package cn.chauncy.component;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.google.inject.Inject;

public class BaseJacksonTypeHandler extends JacksonTypeHandler {
    @Inject
    public BaseJacksonTypeHandler() {
        super(Object.class);
    }
}