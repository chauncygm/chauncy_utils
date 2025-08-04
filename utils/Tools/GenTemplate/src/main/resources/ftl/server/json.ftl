{
<#list data.sheetContent.dataInfoList as row>
    "${row.id}": {
        "@type": "${package}.bean.Cfg${data.sheetName?cap_first}",
    <#list row.cellValueMap?values as cell>
        <#-- 数组 -->
        <#if cell.fieldInfo.arrayType>
        "${cell.fieldInfo.name}" : [
            <#-- 对象数组 -->
            <#if cell.fieldInfo.objectType>
                <#list cell.fieldValue as value>
            {
            <#list cell.fieldInfo.javaFieldMap?keys as fieldName>
                "${fieldName}": ${value[fieldName_index]}<#if fieldName_has_next == true>,</#if>
            </#list>
            }<#if value_has_next == true>,</#if>
                </#list>
            <#-- 普通数组 -->
            <#else>
                <#list cell.fieldValue as value>
                    ${value}<#if value_has_next == true>,</#if>
                </#list>
            </#if>
        ]<#if cell_has_next == true>,</#if>
        <#-- 非数组类型 -->
        <#else>
            <#-- 对象类型 -->
            <#if cell.fieldInfo.objectType>
        {
            <#list cell.fieldInfo.javaFieldMap?keys as fieldName>
            "${fieldName.name}": ${cell.fieldValue[fieldName_index]}<#if fieldName_has_next == true>,</#if>
            </#list>
        }<#if cell_has_next == true>,</#if>
            <#-- 基本类型 -->
            <#else>
        "${cell.fieldInfo.name}": ${cell.fieldValue}<#if cell_has_next == true>,</#if>
            </#if>
        </#if>
    </#list>
    }<#if row_has_next == true>,</#if>
</#list>
}