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
<#list datalist as data>
        // ID:${data.id} 字段数:${data.col} 有效数据行数:${data.row} 说明:${data.desc}
        initLoad(Cfg${data.name?cap_first}.TABLE_NAME);
</#list>
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
<#list datalist as data>
    <#if data.id != 97>
            // ID:${data.id} 字段数:${data.col} 有效数据行数:${data.row} 说明:${data.desc}
            case Cfg${data.name?cap_first}.TABLE_NAME -> Cfg${data.name?cap_first}.reload(data);
    </#if>
</#list>
            default -> -1;
        };
    }
}