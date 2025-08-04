package cn.chauncy.reader;

import cn.chauncy.exception.ExcelScanException;
import cn.chauncy.option.ExportOption;
import cn.chauncy.struct.SheetContent;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SheetReader {

    protected static final Logger logger = LoggerFactory.getLogger(SheetReader.class);

    protected final String sheetInfo;

    public SheetReader(String sheetInfo) {
        this.sheetInfo = sheetInfo;
    }

    public abstract SheetContent read(Sheet sheet, ExportOption option);

    protected int getTotalRowCount(Sheet sheet) {
        return sheet.getLastRowNum() + 1;
    }

    protected Int2ObjectMap<String> getCellMapOfSheet(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            throw new ExcelScanException("[" + sheetInfo + "] sheet row is null, index: " + rowIndex);
        }
        return getCellIndexValueMapOfRow(row);
    }

    protected Int2ObjectMap<String> getCellIndexValueMapOfRow(Row row) {
        int totalCell = getTotalCellCountOfRow(row);
        Int2ObjectMap<String> cellValueMap = new Int2ObjectArrayMap<>(totalCell);
        for (int index = 0; index < totalCell; index++) {
            cellValueMap.put(index, readCellValue(row, index));
        }
        return cellValueMap;
    }

    protected int getTotalCellCountOfRow(Row row) {
        return row.getLastCellNum();
    }

    protected int findColIndexOfName(Int2ObjectMap<String> cellIndexValueMap, String name) {
        for (Int2ObjectMap.Entry<String> entry : cellIndexValueMap.int2ObjectEntrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getIntKey();
            }
        }
        return -1;
    }

    protected String readCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return "";
        }

        String value;
        switch (cell.getCellType()) {
            case STRING -> value = cell.getStringCellValue();
            case NUMERIC -> {
                // 数值若是整数，则取整，避免出现10.0
                double numericCellValue = cell.getNumericCellValue();
                if (numericCellValue == (long) numericCellValue) {
                    value = String.valueOf((long)numericCellValue);
                } else {
                    value = String.valueOf(numericCellValue);
                }
            }
            case BOOLEAN -> value = String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> value = cell.getCellFormula();
            case BLANK -> value = "";
            case ERROR -> value = "error";
            default -> value = cell.toString().trim();
        }
        return value.equalsIgnoreCase("null") ? "" : value.trim();
    }


}
