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
        // ID:20 字段数:11 有效数据行数:8 说明:任务
        initLoad(CfgTask.TABLE_NAME);
        // ID:21 字段数:5 有效数据行数:6 说明:条件
        initLoad(CfgCondition.TABLE_NAME);
        // ID:11 字段数:7 有效数据行数:18 说明:背包
        initLoad(CfgBag.TABLE_NAME);
        // ID:12 字段数:7 有效数据行数:18 说明:道具
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
            // ID:20 字段数:11 有效数据行数:8 说明:任务
            case CfgTask.TABLE_NAME -> CfgTask.reload(data);
            // ID:21 字段数:5 有效数据行数:6 说明:条件
            case CfgCondition.TABLE_NAME -> CfgCondition.reload(data);
            // ID:11 字段数:7 有效数据行数:18 说明:背包
            case CfgBag.TABLE_NAME -> CfgBag.reload(data);
            // ID:12 字段数:7 有效数据行数:18 说明:道具
            case CfgItem.TABLE_NAME -> CfgItem.reload(data);
            default -> -1;
        };
    }
}