package com.auto_generate.bean;

import com.base.BaseBean;
import base.IntKeyValue;
import base.IntKeyLongVal;
import base.IntKeyFloatVal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import utils.JacksonUtil;

import java.util.LinkedHashMap;

/**
 * 说明: 表ID:${tc.id} ${tc.desc}
 * Created on 2020-10-29 , 0029 21:01
 *
 * @author tiancheng <276013558@qq.com>
 */
public class Cfg${tc.name?cap_first} extends BaseBean {
    /**
     * ID:${tc.id} 字段数:${tc.col} 有效数据行数:${tc.row} 说明:${tc.desc}
     */
    @JsonIgnore
    public final static String TABLE_NAME = "${tc.name}";

    /**
     * data
     */
    @JsonIgnore
    private static LinkedHashMap<Integer, Cfg${tc.name?cap_first}> dataMap = null;

    /**
     * 通过key查询
     *
     * @param key 配置表key
     * @return
     */
    public static Cfg${tc.name?cap_first} get(int key) {
        return dataMap.get(key);
    }

    /**
     * 遍历
     *
     * @return
     */
    public static LinkedHashMap<Integer, Cfg${tc.name?cap_first}> all() {
        return dataMap;
    }

    /**
     * 重载配置
     *
     * @param data 配置数据
     * @return
     * @throws JsonProcessingException
     */
    public static int reload(String data) throws JsonProcessingException {
        if (data == null || "".equals(data)) {
            dataMap.clear();
            return 0;
        }

        dataMap = JacksonUtil.jsonToObject(data, new TypeReference<LinkedHashMap<Integer, Cfg${tc.name?cap_first}>>() {
        });
        return dataMap.size();
    }

    private Cfg${tc.name?cap_first}() { }

<#list tc.cols as col>
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

<#list tc.cols as col>
    /**
     * ${col.desc}
${col.descT}
     */
    private ${col.type} ${col.name};
</#list>

<#list tc.cols as col>
    /**
     * ${col.desc}
     */
    public ${col.type} get${col.name?cap_first}() {
        return ${col.name};
    }
</#list>
}