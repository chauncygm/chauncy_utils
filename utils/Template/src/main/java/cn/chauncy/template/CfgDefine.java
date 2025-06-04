package cn.chauncy.template;

import cn.chauncy.template.bean.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

public abstract class CfgDefine {
    /**
     * 初始化指定配置
     *
     * @param tableName 表格名字
     */
    protected abstract void initLoad(String tableName) throws IOException;

    public void init() throws IOException {
        // ID:10 字段数:1 有效数据行数:1 说明:全局表
        initLoad(CfgGlobal.TABLE_NAME);
        // ID:11 字段数:4 有效数据行数:6 说明:道具表
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
        return switch (tableName) {
            // ID:10 字段数:1 有效数据行数:1 说明:全局表
            case CfgGlobal.TABLE_NAME -> CfgGlobal.reload(data);
            // ID:11 字段数:4 有效数据行数:6 说明:道具表
            case CfgItem.TABLE_NAME -> CfgItem.reload(data);
            default -> -1;
        };
    }
}