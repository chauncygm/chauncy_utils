package ${package};

import ${package}.bean.*;
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
<#list data as info>
    <#if info.sheetId != 99>
        // ID:${info.sheetId} 字段数:${info.sheetContent.fieldInfoMap?size} 有效数据行数:${info.sheetContent.dataInfoList?size} 说明:${info.sheetComment}
        initLoad(Cfg${info.sheetName?cap_first}.TABLE_NAME);
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
        return switch (tableName) {
<#list data as info>
    <#if info.sheetId != 99>
            // ID:${info.sheetId} 字段数:${info.sheetContent.fieldInfoMap?size} 有效数据行数:${info.sheetContent.dataInfoList?size} 说明:${info.sheetComment}
            case Cfg${info.sheetName?cap_first}.TABLE_NAME -> Cfg${info.sheetName?cap_first}.reload(data);
    </#if>
</#list>
            default -> -1;
        };
    }
}