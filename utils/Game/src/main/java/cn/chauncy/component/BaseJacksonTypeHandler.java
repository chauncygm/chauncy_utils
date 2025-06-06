package cn.chauncy.component;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

public class BaseJacksonTypeHandler extends JacksonTypeHandler {
    public BaseJacksonTypeHandler() {
        super(Object.class);
    }
}