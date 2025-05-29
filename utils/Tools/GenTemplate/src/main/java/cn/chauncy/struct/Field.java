package cn.chauncy.struct;

import lombok.Data;

@Data
public class Field {
    private String type;
    private String name;

    public Field(String type, String name) {
        this.type = type;
        this.name = name;
    }

}
