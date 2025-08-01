package cn.chauncy.template.bean1;

import java.util.*;
import cn.chauncy.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
<#-- 考虑优化，根据需要写入这条import -->
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cn.chauncy.base.ImmutableListDeserializer;
import lombok.Getter;

/**
 * 说明: ${data.sheetComment}表 ID:${data.sheetId} 字段数:${data.sheetContent.fieldInfoMap?size} 有效数据行数:${data.sheetContent.dataInfoList?size}
 * Created on ${.now?string("yyyy-MM-dd HH:mm")}
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public class Cfg${data.sheetName?cap_first} {

    private Cfg${data.sheetName?cap_first}() {}

<#-- 生成主类字段 -->
<#list data.sheetContent.fieldInfoMap?values as field>
    /** ${field.comment} */
    <#if field.javaType?starts_with("List")>
    @JsonDeserialize(using = ImmutableListDeserializer.class)
    </#if>
    private ${field.javaType} ${field.name};
</#list>

<#-- 生成内部类 -->
<#list data.sheetContent.fieldInfoMap?values as fieldInfo>
    <#if fieldInfo.javaFieldMap?size gt 0>
    @Getter
    public static class ${fieldInfo.javaType?replace("List<", "")?replace(">", "")} {
    <#list fieldInfo.javaFieldMap?keys as name>
        private ${fieldInfo.javaFieldMap[name]} ${name};
    </#list>
    }
    </#if>
</#list>

    public static final String TABLE_NAME = "${data.sheetName}";
    private static Map<Integer, Cfg${data.sheetName?cap_first}> dataMap = Map.of();
    private static final TypeReference<LinkedHashMap<Integer, Cfg${data.sheetName?cap_first}>> MAP_REFERENCE = new TypeReference<>() {};

    public static Cfg${data.sheetName?cap_first} get(int key) {
        return dataMap.get(key);
    }

    public static Map<Integer, Cfg${data.sheetName?cap_first}> all() {
        return dataMap;
    }

    public static int reload(String data) {
        if (data == null || data.isEmpty()) {
            dataMap.clear();
            return 0;
        }
        dataMap = JsonUtils.readFromJson(data, MAP_REFERENCE);
        dataMap = Collections.unmodifiableMap(dataMap);
        return dataMap.size();
    }
}