package cn.chauncy.template;

import cn.chauncy.template.bean.*;
import com.fasterxml.jackson.core.JsonProcessingException;

public abstract class CfgDefine {
    /**
     * 初始化指定配置
     *
     * @param tableName
     */
    protected abstract void initLoad(String tableName);

    public void init() {
        // ID:10 字段数:4 有效数据行数:6 说明:道具
        initLoad(CfgItem.TABLE_NAME);
    }
	
	/**
     * 加载配置
     *
     * @param tableName
     * @param data
     * @return
     * @throws JsonProcessingException
     */
    public int reloadCfg(String tableName, String data) throws JsonProcessingException {
        switch (tableName) {
            // ID:10 字段数:4 有效数据行数:6 说明:道具
            case CfgItem.TABLE_NAME:
                return CfgItem.reload(data);
            default:
                return -1;
        }
    }
}