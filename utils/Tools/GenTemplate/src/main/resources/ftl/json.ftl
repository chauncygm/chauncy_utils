{
<#list data.cols[0].values as key>
    <#if data.id == 98>
    "1": {
    <#else>
    "${key[0][0]}": {
    </#if>
        "@type": "cn.chauncy.template.bean.Cfg${data.name?cap_first}",
    <#list data.cols as col>
        <#if "${col.wei}" == "0">
        <#--基本类型-->
            <#if "${col.type}" == "String">
        "${col.name}": "${col.values[key_index][0][0]}"<#if col_has_next == true>,</#if>
            <#else>
        "${col.name}": ${col.values[key_index][0][0]}<#if col_has_next == true>,</#if>
            </#if>
        <#elseif "${col.wei}" == "1">
        <#--数组基本类型-->
        "${col.name}": [
            <#list col.values[key_index] as g>
            ${g[0]}<#if g_has_next == true>,</#if>
            </#list>
        ]<#if col_has_next == true>,</#if>
        <#elseif "${col.wei}" == "2">
        <#--对象-->
        "${col.name}": {
            <#if col.specialType == 1>
            "@type": "base.Entry.Int2IntVal",
            </#if>
            <#list col.values[key_index] as g>
                <#list col.fields as f>
                    <#if "${f.type}" == "String">
            "${f.name}": "${g[f_index]}"<#if f_has_next == true>,</#if>
                    <#else>
            "${f.name}": ${g[f_index]}<#if f_has_next == true>,</#if>
                    </#if>
                </#list>
            </#list>
        }<#if col_has_next == true>,</#if>
        <#elseif "${col.wei}" == "3">
        <#--数组对象-->
        "${col.name}": [
            <#list col.values[key_index] as g>
            {
                <#if col.specialType == 1>
                "@type": "base.Entry.Int2IntVal",
                </#if>
                <#list col.fields as f>
                    <#if "${f.type}" == "String">
                "${f.name}": "${g[f_index]}"<#if f_has_next == true>,</#if>
                    <#else>
                "${f.name}": ${g[f_index]}<#if f_has_next == true>,</#if>
                    </#if>
                </#list>
            }<#if g_has_next == true>,</#if>
            </#list>
        ]<#if col_has_next == true>,</#if>
        </#if>
    </#list>
    }<#if key_has_next == true>,</#if>
</#list>
}