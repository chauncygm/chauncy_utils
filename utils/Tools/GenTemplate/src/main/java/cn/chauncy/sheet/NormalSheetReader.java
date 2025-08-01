package cn.chauncy.sheet;

import cn.chauncy.struct.CellInfo;
import cn.chauncy.struct.DataInfo;
import cn.chauncy.struct.FieldInfo;
import cn.chauncy.struct.SheetContent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.apache.poi.ss.usermodel.Sheet;

public class NormalSheetReader extends SheetReader {

    private static final int COMMENT_ROW_INDEX = 0;
    private static final int NAME_ROW_INDEX = 1;
    private static final int TYPE_ROW_INDEX = 2;
    private static final int FLAG_ROW_INDEX = 3;
    private static final int DATA_ROW_START_INDEX = 4;

    public NormalSheetReader(String sheetInfo) {
        super(sheetInfo);
    }

    @Override
    public SheetContent read(Sheet sheet) {
        Int2ObjectMap<String> commentMap = getCellMapOfSheet(sheet, COMMENT_ROW_INDEX);
        Int2ObjectMap<String> nameMap = getCellMapOfSheet(sheet, NAME_ROW_INDEX);
        Int2ObjectMap<String> typeMap = getCellMapOfSheet(sheet, TYPE_ROW_INDEX);
        Int2ObjectMap<String> flagMap = getCellMapOfSheet(sheet, FLAG_ROW_INDEX);

        // 读取字段信息
        SheetContent sheetContent = new SheetContent();
        for (Int2ObjectMap.Entry<String> entry : nameMap.int2ObjectEntrySet()) {
            int index = entry.getIntKey();
            String name = entry.getValue();
            FieldInfo fieldInfo = new FieldInfo(name, typeMap.get(index), flagMap.get( index), commentMap.get(index), index);
            sheetContent.getFieldInfoMap().put(fieldInfo.getName(), fieldInfo);
        }

        // 遍历行读取数据信息
        int totalRow = getTotalRowCount(sheet);
        for (int rowIndex = DATA_ROW_START_INDEX; rowIndex < totalRow; rowIndex++) {
            Int2ObjectMap<String> dataMap = getCellMapOfSheet(sheet, rowIndex);
            if (dataMap.isEmpty()) {
                logger.warn("[{}] data row is empty, rowIndex: {}", sheetInfo, rowIndex);
                continue;
            }
            DataInfo dataInfo = new DataInfo(rowIndex);
            for (FieldInfo fieldInfo : sheetContent.getFieldInfoMap().values()) {
                int index = fieldInfo.getIndex();
                String value = dataMap.getOrDefault(index, "");
                Object fieldValue = fieldInfo.getParser().parseValue(value);
                dataInfo.getCellValueMap().put(fieldInfo.getName(), new CellInfo(fieldInfo, value, fieldValue));
            }
            sheetContent.getDataInfoList().add(dataInfo);
        }
        return sheetContent;
    }
}
