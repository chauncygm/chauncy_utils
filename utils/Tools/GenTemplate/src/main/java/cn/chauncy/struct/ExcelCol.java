package cn.chauncy.struct;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExcelCol {
    /**
     * 特殊类型
     * 1:Key(int),Value(int)
     */
    private int specialType;
    private int col;
    private String descT;
    private String desc;
    private String name;
    private String type;
    private String typeClass;
    /**
     * 维，0基本类型，1数组基本类型，2对象，3数组对象
     */
    private int wei;
    /**
     * 如果是对象，则有对象的字段信息
     */
    private List<Field> fields = new ArrayList<>();
    /**
     * 每行数据
     */
    private List<List<List<String>>> values = new ArrayList<>();

}
