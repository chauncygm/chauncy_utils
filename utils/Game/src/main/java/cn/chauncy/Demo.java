package cn.chauncy;

import com.chauncy.utils.mapper.AutoMapper;
import com.chauncy.utils.eventbus.Subscribe;

@AutoMapper(baseMapper = BaseMapper.class)
public class Demo {

    @Subscribe
    public void test(String abc) {
        System.out.println("test");
    }
}
