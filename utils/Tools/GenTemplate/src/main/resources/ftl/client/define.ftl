namespace ${namespace}
{
    public interface CfgDefine {

        protected void InitLoad(string tableName);

        public void Init()
        {
<#list data as info>
    <#if info.sheetId != 99>
            // ID:${info.sheetId} 字段数:${info.sheetContent.fieldInfoMap?size} 有效数据行数:${info.sheetContent.dataInfoList?size} 说明:${info.sheetComment}
            InitLoad(Cfg${info.sheetName?cap_first}.TableName)
    </#if>
</#list>
        }


        public int ReloadCfg(string tableName, string data)
        {
            return tableName switch
            {
                CfgItem.TableName => CfgItem.Reload(data),
<#list data as info>
    <#if info.sheetId != 99>
                // ID:${info.sheetId} 字段数:${info.sheetContent.fieldInfoMap?size} 有效数据行数:${info.sheetContent.dataInfoList?size} 说明:${info.sheetComment}
                Cfg${info.sheetName?cap_first}.TableName => Cfg${info.sheetName?cap_first}.Reload(data),
    </#if>
</#list>
                _ => -1
            };
        }
    }
}