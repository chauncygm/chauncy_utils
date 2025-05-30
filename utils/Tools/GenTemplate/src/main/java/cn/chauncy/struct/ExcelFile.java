package cn.chauncy.struct;

import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
public class ExcelFile {

    private int id;
    private String name;
    private String desc;
    private String path;
    private int byteSize;

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

    private List<ExcelCol> cols = new ArrayList<>();

    public ExcelFile(File file, String name) {
        String[] strings = name.split("_");
        this.id = Integer.parseInt(strings[0]);
        this.name = strings[1];
        this.desc = strings[2];
        this.path = file.getAbsolutePath();
    }

    @Override
    public String toString() {
        return "TableFile{" +
                "path='" + path + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

    public String info() {
        return "name='" + name + '\'' +
                ", id=" + id +
                ", desc='" + desc + '\'';
    }
}
