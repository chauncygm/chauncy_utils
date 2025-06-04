package cn.chauncy.component;

import cn.chauncy.utils.time.CachedTime;
import cn.chauncy.utils.time.ShiftableTimeProvider;

public class GlobalTimeProvider extends ShiftableTimeProvider {

    public GlobalTimeProvider() {
        super(new CachedTime());
    }
}
