package cn.chauncy.parser;

import java.util.Map;
import java.util.Set;

public class BaseTypeParser implements FieldParser {

    private final String type;

    private BaseTypeParser(String type) {
        type = type.toLowerCase().trim();
        Set<String> baseTypeSet = Set.of("int", "long", "float", "string");
        if (!baseTypeSet.contains(type)) {
            throw new IllegalArgumentException("BaseTypeParser only support int, long, float, string");
        }
        this.type = type;
    }

    @Override
    public String javaType() {
        return type.equals("string") ? "String" : type;
    }

    @Override
    public String cSharpType() {
        return type;
    }

    @Override
    public Map<String, String> getJavaTypeFieldMap() {
        return Map.of();
    }

    @Override
    public Map<String, String> getCSharpTypeFieldMap() {
        return Map.of();
    }

    @Override
    public Object parseValue(String value) {
        value = value.trim();
        String numValue = value.isEmpty() ? "0" : value;
        return switch (type) {
            case "int" -> Integer.valueOf(numValue);
            case "long" -> Long.valueOf(numValue);
            case "float" -> Float.valueOf(numValue) + "f";
            case "string" -> "\"" + value + "\"";
            default -> null;
        };
    }

    private static final BaseTypeParser INT_PARSER = new BaseTypeParser("int");
    private static final BaseTypeParser FLOAT_PARSER = new BaseTypeParser("float");
    private static final BaseTypeParser LONG_PARSER = new BaseTypeParser("long");
    private static final BaseTypeParser STRING_PARSER = new BaseTypeParser("string");

    public static BaseTypeParser getParser(String type) {
        type = type.toLowerCase();
        return switch (type) {
            case "int", "int32" -> INT_PARSER;
            case "long", "int64" -> LONG_PARSER;
            case "float" -> FLOAT_PARSER;
            case "string" -> STRING_PARSER;
            default -> null;
        };
    }

    public static String getWrapperType(String type) {
        type = type.toLowerCase();
        return switch (type) {
            case "int" -> "Integer";
            case "long" -> "Long";
            case "float" -> "Float";
            case "string" -> "String";
            default -> null;
        };
    }
}
