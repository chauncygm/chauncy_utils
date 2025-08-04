package cn.chauncy.parser;

import cn.chauncy.exception.ExcelParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.chauncy.util.ExcelUtil.firstCapital;

public class ObjectFieldParser implements FieldParser {

    private final String fieldName;
    private final List<FieldParser> fieldParsers = new ArrayList<>();
    /** 对象类型字段的属性信息，key属性名,value属性类型 */
    private final Map<String, String> javaTypeFieldsMap = new Object2ObjectArrayMap<>();
    private final Map<String, String> cSharpTypeFieldsMap = new Object2ObjectArrayMap<>();

    private String specialType;

    public ObjectFieldParser(String fieldName, String type) {
        this.fieldName = fieldName;
        String fieldString = type.substring(1, type.length() - 1);
        String[] fieldArray = fieldString.split(";");
        for (String field : fieldArray) {
            String[] fieldSplit = field.split(" ");
            FieldParser parser = FieldParser.getParser(fieldName, fieldSplit[0]);
            if (!(parser instanceof BaseTypeParser)) {
                throw new ExcelParseException("对象字段类型不是基本类型！");
            }
            fieldParsers.add(parser);
            javaTypeFieldsMap.put(fieldSplit[1], parser.javaType());
            cSharpTypeFieldsMap.put(fieldSplit[1], parser.cSharpType());
        }

        if (fieldParsers.size() == 2 && "int".equals(fieldParsers.get(0).javaType())) {
            switch (fieldParsers.get(1).javaType()) {
                case "int": specialType = "Entry.Int2IntVal";break;
                case "float": specialType = "Entry.Int2FloatVal";break;
                case "long": specialType = "Entry.Int2LongVal";break;
            }
            if (specialType != null) {
                javaTypeFieldsMap.clear();
                javaTypeFieldsMap.put("k", fieldParsers.get(0).javaType());
                javaTypeFieldsMap.put("v", fieldParsers.get(1).javaType());
            }
        }
    }

    @Override
    public String javaType() {
        if (specialType != null) {
            return specialType;
        }
        return "D" + firstCapital(fieldName);
    }

    @Override
    public String cSharpType() {
        return "D" + firstCapital(fieldName);
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public Map<String, String> getJavaTypeFieldMap() {
        return javaTypeFieldsMap;
    }

    @Override
    public Map<String, String> getCSharpTypeFieldMap() {
        return cSharpTypeFieldsMap;
    }

    @Override
    public Object parseValue(String value) {
        if (value.isEmpty()) {
            return List.of();
        }
        String[] split = value.split(",");
        if (split.length != fieldParsers.size()) {
            throw new ExcelParseException("对象字段不匹配！");
        }
        List<Object> list = new ArrayList<>(fieldParsers.size());
        for (int i = 0; i < fieldParsers.size(); i++) {
            FieldParser parser = fieldParsers.get(i);
            list.add(parser.parseValue(split[i]));
        }
        return list;
    }
}
