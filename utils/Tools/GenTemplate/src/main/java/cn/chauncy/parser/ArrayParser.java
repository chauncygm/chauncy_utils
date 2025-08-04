package cn.chauncy.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArrayParser implements FieldParser {

    private final FieldParser subFieldParser;

    public ArrayParser(String fieldName, String type) {
        type = type.trim();
        String subType = type.substring(2);
        this.subFieldParser = FieldParser.getParser(fieldName, subType);
    }

    @Override
    public String javaType() {
        if (subFieldParser instanceof BaseTypeParser) {
            return "List<" + BaseTypeParser.getWrapperType(subFieldParser.javaType()) + ">";
        }
        return "List<" + subFieldParser.javaType() + ">";
    }

    @Override
    public String cSharpType() {
        return "List<" + subFieldParser.cSharpType() + ">";
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isObject() {
        return subFieldParser.isObject();
    }

    @Override
    public Map<String, String> getJavaTypeFieldMap() {
        if (subFieldParser instanceof ObjectFieldParser) {
            return subFieldParser.getJavaTypeFieldMap();
        }
        return Map.of();
    }

    @Override
    public Map<String, String> getCSharpTypeFieldMap() {
        if (subFieldParser instanceof ObjectFieldParser) {
            return subFieldParser.getCSharpTypeFieldMap();
        }
        return Map.of();
    }

    @Override
    public Object parseValue(String value) {
        value = value.trim();
        if (value.isEmpty()) {
            return List.of();
        }
        String[] values = value.split("\\|");
        List<Object> list = new ArrayList<>(values.length);
        for (String v : values) {
            Object o = subFieldParser.parseValue(v);
            list.add(o);
        }
        return list;
    }
}
