package cn.chauncy;

import cn.chauncy.annotation.AutoMapper;
import cn.chauncy.annotation.Subscribe;

@AutoMapper(baseMapper = BaseMapper.class)
public class Demo {

    @Subscribe
    private void test() {
        System.out.println("test");
    }
}
