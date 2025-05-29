package cn.chauncy.struct;

import lombok.Data;

import java.util.List;

@Data
public class TableFile {
    private String path;
    private String name;
    private int id;
    private String desc;
    /**
     * 有效行数据
     */
    private int row;
    /**
     * 有效列数据，如果为0，则不会给服务器导此表
     */
    private int col;
    /**
     * 所有列，包含客户端
     */
    private int allCol;
    /**
     * 所有行
     */
    private int allRow;
    private List<ExcelCol> cols;
    /**
     * 不是服务器的表
     */
    private boolean notSrv = false;
    /**
     * FileInputStream available
     */
    private int size;

    public TableFile(String path, String name) {
        this.path = path;
        String[] strings = name.split("_");
        this.name = strings[1];
        this.id = Integer.parseInt(strings[0]);

        StringBuilder desc = new StringBuilder();
        for (int i = 2; i < strings.length; i++) {
            desc.append(strings[i]);
            if (i != strings.length - 1)
                desc.append("_");
        }
        this.desc = desc.toString();
    }

    @Override
    public String toString() {
        return "TableFile{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", id=" + id +
                ", desc='" + desc + '\'' +
                '}';
    }

    public String toString2() {
        return " name='" + name + '\'' +
                ", id=" + id +
                ", desc='" + desc + '\'';
    }
}
