package cn.chauncy.struct;

import cn.chauncy.parser.FieldParser;
import lombok.Data;

import java.util.Map;

@Data
public class FieldInfo {

    /** 字段名 */
    private String name;
    /** 类型 */
    private String type;
    private String flags;
    private String comment;

    /** 行或列的索引 */
    private int index;

    /** 类型数据解析信息 */
    private String javaType;
    private String cSharpType;
    private boolean arrayType;
    private boolean objectType;
    private String subType;
    private Map<String, String> javaFieldMap;
    private Map<String, String> cSharpFieldMap;
    private FieldParser parser;

    public FieldInfo(String name, String type, String flags, String comment, int index) {
        this.name = name;
        this.type = type;
        this.flags = flags;
        this.comment = comment;
        this.index = index;
        this.parser = FieldParser.getParser(name, type);
        this.javaType = parser.javaType();
        this.cSharpType = parser.cSharpType();
        this.arrayType = parser.isArray();
        this.objectType = parser.isObject();
        this.javaFieldMap = parser.getJavaTypeFieldMap();
        this.javaFieldMap = parser.getJavaTypeFieldMap();
    }
}
