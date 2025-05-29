package com.auto_generate;

import com.auto_generate.bean.*;
import com.fasterxml.jackson.core.JsonProcessingException;

<#list data as tc>
    <#if tc.id != 97>
import com.auto_generate.bean.Cfg${tc.name?cap_first};
    </#if>
</#list>

public abstract class CfgDefine {
    /**
     * 初始化指定配置
     *
     * @param tableName
     */
    protected abstract void initLoad(String tableName);

    public void init() {
<#list data as tc>
    <#if tc.id != 97>
        // ID:${tc.id} 字段数:${tc.col} 有效数据行数:${tc.row} 说明:${tc.desc}
        initLoad(Cfg${tc.name?cap_first}.TABLE_NAME);
    </#if>
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
        switch (tableName) {
<#list data as tc>
    <#if tc.id != 97>
            // ID:${tc.id} 字段数:${tc.col} 有效数据行数:${tc.row} 说明:${tc.desc}
            case Cfg${tc.name?cap_first}.TABLE_NAME:
                return Cfg${tc.name?cap_first}.reload(data);
    </#if>
</#list>
            default:
                return -1;
        }
    }
}