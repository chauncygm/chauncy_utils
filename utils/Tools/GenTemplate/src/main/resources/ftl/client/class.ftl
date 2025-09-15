using System.Collections.Generic;
using GameProto.Base;
using Newtonsoft.Json;

namespce ${namespace}
{
    ///<summary>
    /// 说明: ${data.sheetComment}表 ID:${data.sheetId} 字段数:${data.sheetContent.fieldInfoMap?size} 有效数据行数:${data.sheetContent.dataInfoList?size}
    /// Created on ${.now?string("yyyy-MM-dd HH:mm")}
    ///</summary>
    public class Cfg${data.sheetName?cap_first} : ConfigBase<Cfg${data.sheetName?cap_first}>
    {

        public const string TableName = "${data.sheetName}";

        [JsonConstructor]
        private Cfg${data.sheetName?cap_first}(<#list data.sheetContent.fieldInfoMap?values as field>[JsonProperty(nameof(field.name?cap_first))] ${field.csharpType} ${field.name}<#if field_has_next>,
        </#if></#list>)
        {
            <#list data.sheetContent.fieldInfoMap?values as field>
            ${field.name?cap_first} = ${field.name};
            </#list>
        }

    <#-- 生成类字段 -->
    <#list data.sheetContent.fieldInfoMap?values as field>
        <#if field.name != "id">
        /// <summary>
        /// ${field.comment}
        /// </summary>
            <#if field.arrayType>
        [JsonConverter(typeof(ImmutableListConverter))]
            </#if>
        private ${field.csharpType} ${field.name?cap_first} { get; }
        </#if>
    </#list>

<#if data.sheetContent.fieldInfoMap?size gt 0>
    <#-- 生成内部类 -->
    <#list data.sheetContent.fieldInfoMap?values as fieldInfo>
        <#if fieldInfo.csharpType?starts_with("D") || fieldInfo.csharpType?starts_with("IList<D")>
        public class ${fieldInfo.csharpType?replace("IList<", "")?replace(">", "")}
        {
        <#list fieldInfo.cSharpFieldMap?keys as name>
            private ${fieldInfo.cSharpFieldMap[name]} ${name?cap_first} { get; }
        </#list>
        }
        </#if>
    </#list>
</#if>
    }
}