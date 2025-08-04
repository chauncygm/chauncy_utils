package cn.chauncy.parser;

import java.util.Map;

/**
 * 表格字段解析器
 * <p>
 * 4种类型：
 *    <li>基本类型      int, long, float, string
 *    <li>对象类型      {type1:name1;type2:name2;...}
 *    <li>基本类型数组   []int, []long, []float, []string
 *    <li>对象类型数组   []{type1:name1;type2:name2;...}
 */
public interface FieldParser {

    /** 获取字段的java类型，用于生成代码 */
    String javaType();

    /** 获取字段的c#类型，用于生成代码 */
    String cSharpType();

    /** 是否是数组 */
    default boolean isArray() {
        return false;
    }

    /** 元素是否是对象 */
    default boolean isObject() {
        return false;
    }

    /** 获取自定义类型的java字段信息，当字段是对象或对象数组类型才存在 */
    Map<String, String> getJavaTypeFieldMap();

    /** 获取自定义类型的c#字段信息，当字段是对象或对象数组类型才存在 */
    Map<String, String> getCSharpTypeFieldMap();

    /** 解析配置的数据 */
    Object parseValue(String value);

    static FieldParser getParser(String fieldName, String type) {
        type = type.trim();
        if (type.startsWith("[]")) {
            return new ArrayParser(fieldName, type);
        }
        if (type.startsWith("{") && type.endsWith("}")) {
            return new ObjectFieldParser(fieldName, type);
        }
        return BaseTypeParser.getParser(type);
    }

}
