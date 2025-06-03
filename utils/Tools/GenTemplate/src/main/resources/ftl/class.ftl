package cn.chauncy.template.bean;

import java.util.*;
import cn.chauncy.base.Entry;
import cn.chauncy.base.BaseBean;
import cn.chauncy.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 说明: 表ID:${data.id} ${data.desc}
 * Created on ${.now?string("yyyy-MM-dd HH:mm")}
 */
public class Cfg${data.name?cap_first} extends BaseBean {
    /**
     * ID:${data.id} 字段数:${data.col} 有效数据行数:${data.row} 说明:${data.desc}
     */
    @JsonIgnore
    public final static String TABLE_NAME = "${data.name}";

    /**
     * data
     */
    @JsonIgnore
    private static Map<Integer, Cfg${data.name?cap_first}> dataMap = null;

    /**
     * 通过key查询
     *
     * @param key 配置表key
     * @return
     */
    public static Cfg${data.name?cap_first} get(int key) {
        return dataMap.get(key);
    }

    /**
     * 遍历
     *
     * @return
     */
    public static Map<Integer, Cfg${data.name?cap_first}> all() {
        return dataMap;
    }

    /**
     * 重载配置
     *
     * @param data 配置数据
     * @return
     */
    public static int reload(String data) {
        if (data == null || data.isEmpty()) {
            dataMap.clear();
            return 0;
        }

        dataMap = JsonUtils.readFromJson(data, new TypeReference<LinkedHashMap<Integer, Cfg${data.name?cap_first}>>() {});
        dataMap = Collections.unmodifiableMap(dataMap);
        return dataMap.size();
    }

    private Cfg${data.name?cap_first}() { }

<#list data.cols as col>
    <#if col.specialType == 0>
    <#if col.wei == 2 || col.wei == 3>
    /**
     * ${col.desc}
     */
    public static class ${col.typeClass} {
        <#list col.fields as field>
        private ${field.type} ${field.name};
        public ${field.type} get${field.name?cap_first}() {
            return ${field.name};
        }

        </#list>
    }
    </#if>
    </#if>
</#list>

<#list data.cols as col>
    /**
     * ${col.desc} ${col.descT}
     */
    private ${col.type} ${col.name};
</#list>

<#list data.cols as col>
    /**
     * ${col.desc}
     */
    public ${col.type} get${col.name?cap_first}() {
        return ${col.name};
    }
</#list>
}