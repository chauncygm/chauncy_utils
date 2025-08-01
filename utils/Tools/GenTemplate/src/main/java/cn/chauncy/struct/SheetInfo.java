package cn.chauncy.struct;

import lombok.Data;

import java.io.File;

@Data
public class SheetInfo {

    /** 表格id */
    private int sheetId;
    /** 表格名 */
    private String sheetName;
    /** 表格说明 */
    private String sheetComment;

    /** 表格文件 */
    private File file;

    /** 表格页签索引，默认固定读取第一个表格页 */
    private final int sheetIndex = 0;

    /** 表格内容 */
    private SheetContent sheetContent;

    public SheetInfo() {
    }

    public SheetInfo(File file) {
        this.file = file;
        String fileName = file.getName().split("\\.")[0];
        String[] split = fileName.split("_");
        this.sheetId = Integer.parseInt(split[0]);
        this.sheetName = split[1];
        this.sheetComment = split[2];
    }

    @Override
    public String toString() {
        return "SheetInfo{" +
                "sheetId=" + sheetId +
                ", sheetName='" + sheetName + '\'' +
                ", sheetComment='" + sheetComment + '\'' +
                ", fieldCount: " + (sheetContent == null ? 0 : sheetContent.getFieldInfoMap().size()) +
                ", dataCount: " + (sheetContent == null ? 0 : sheetContent.getDataInfoList().size()) +
                '}';
    }
}
