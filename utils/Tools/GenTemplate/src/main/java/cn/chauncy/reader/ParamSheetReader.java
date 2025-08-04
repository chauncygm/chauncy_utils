package cn.chauncy.reader;

import cn.chauncy.option.ExportOption;
import cn.chauncy.struct.CellInfo;
import cn.chauncy.struct.DataInfo;
import cn.chauncy.struct.FieldInfo;
import cn.chauncy.struct.SheetContent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ParamSheetReader extends SheetReader {

    protected static final String COL_FLAGS = "flags";
    protected static final String COL_NAME = "name";
    protected static final String COL_TYPE = "type";
    protected static final String COL_VALUE = "value";
    protected static final String COL_COMMENT = "comment";

    private static final int nameRowIndex = 0;
    private static final int commentRowIndex = 1;
    private static final int dataRowStartIndex = 2;

    public ParamSheetReader(String sheetInfo) {
        super(sheetInfo);
    }

    @Override
    public SheetContent read(Sheet sheet, ExportOption option) {
        Row nameRow = sheet.getRow(nameRowIndex);
        Int2ObjectMap<String> cellValueMapOfRow = getCellIndexValueMapOfRow(nameRow);
        // 获取字段的列索引
        int nameColIndex = findColIndexOfName(cellValueMapOfRow, COL_NAME);
        int flagsColIndex = findColIndexOfName(cellValueMapOfRow, COL_FLAGS);
        int commentColIndex = findColIndexOfName(cellValueMapOfRow, COL_COMMENT);
        int typeColIndex = findColIndexOfName(cellValueMapOfRow, COL_TYPE);
        int valueColIndex = findColIndexOfName(cellValueMapOfRow, COL_VALUE);

        SheetContent sheetContent = new SheetContent();
        int totalRowCount = getTotalRowCount(sheet);
        DataInfo dataInfo = new DataInfo(0);
        dataInfo.setId(1);
        // 遍历数据行读取字段信息和配置值
        for (int index = dataRowStartIndex; index < totalRowCount; index++) {
            Row row = sheet.getRow(index);
            String name = readCellValue(row, nameColIndex);
            String type = readCellValue(row, typeColIndex);
            String flag = readCellValue(row, flagsColIndex);
            String comment = readCellValue(row, commentColIndex);
            String cellValue = readCellValue(row, valueColIndex);
            // 无需导出的字段
            if (flag.contains(option.getMode().value)) {
                continue;
            }

            FieldInfo fieldInfo = new FieldInfo(name, type, flag, comment, index);
            sheetContent.getFieldInfoMap().put(fieldInfo.getName(), fieldInfo);

            Object fieldValue = fieldInfo.getParser().parseValue(cellValue);
            dataInfo.getCellValueMap().put(fieldInfo.getName(), new CellInfo(fieldInfo, cellValue, fieldValue));
        }
        sheetContent.getDataInfoList().add(dataInfo);
        return sheetContent;
    }

}
