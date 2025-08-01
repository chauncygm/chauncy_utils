package cn.chauncy;

import cn.chauncy.exception.ExcelParseException;
import cn.chauncy.struct.ExcelCol;
import cn.chauncy.struct.ExcelFile;
import cn.chauncy.struct.Field;
import cn.chauncy.struct.SheetInfo;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;

import static cn.chauncy.ExcelUtil.*;


public class ExcelReader {

    private  static final Logger logger = LoggerFactory.getLogger(ExcelReader.class);

    public static final ExcelReader INSTANCE = new ExcelReader();
    private static final NumberFormat numberFormat;
    private static final FileFilter excelFileFilter;
    private static final Pattern excelNamePattern = Pattern.compile("[0-9]+_[a-zA-Z]+_[\\u4e00-\\u9fa5]+");
    private static final int BASE_ROW = 5;

    private static final Set<String> supportBaseTypeSet = Set.of("int", "long", "float");
    private final ThreadLocal<String> curColNameHolder = ThreadLocal.withInitial(() -> "");
    private final ThreadLocal<Integer> curRowNumHolder = ThreadLocal.withInitial(() -> 0);

    static {
        ZipSecureFile.setMinInflateRatio(0.1d);
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(6);
        excelFileFilter = (file) -> {
            if (!file.isFile() || !file.canRead()) {
                return false;
            }
            String filename = file.getName();
            if (filename.startsWith("~")) {
                return false;
            }
            return filename.endsWith(".xlsx") || filename.endsWith(".xls") || filename.endsWith(".xlsm");
        };
    }


    public List<ExcelFile> scanExcelDir(File dir, List<Integer> exportIds) {
        List<ExcelFile> excelFiles = new ArrayList<>();
        Set<String> names = new HashSet<>();
        Set<Integer> ids = new HashSet<>();

        File[] files = dir.listFiles(excelFileFilter);
        for (File f : Objects.requireNonNull(files)) {
            String excelName = getExcelName(f.getName());
            if (!excelNamePattern.matcher(excelName).matches()) {
                System.err.println("表名格式(需满足id_name_描述.xlsx)错误：" + f.getName());
                System.exit(-1);
            }
            ExcelFile excelFile = new ExcelFile(f, excelName);
            if (!exportIds.isEmpty() && !exportIds.contains(excelFile.getId())) {
                continue;
            }
            if (ids.contains(excelFile.getId())) {
                System.err.println("发现重复ID的配置表, ID: " + excelFile.getId());
                System.exit(-1);
            }
            if (names.contains(excelFile.getName())) {
                System.err.println("发现名字重复的配置表, 表名: " + excelFile.getName());
                System.exit(-1);
            }
            ids.add(excelFile.getId());
            names.add(excelFile.getName());
            excelFiles.add(excelFile);
        }
        excelFiles.sort(Comparator.comparingInt(ExcelFile::getId));
        return excelFiles;
    }

    public void parse(List<ExcelFile> excelFiles) {
        Iterator<ExcelFile> iterator = excelFiles.iterator();
        while (iterator.hasNext()) {
            ExcelFile table = iterator.next();
            try {
                parse(table);
            } catch (ExcelParseException e) {
                System.err.println("读取表数据失败{" + e.getMessage() + "}，中止:" + table);
                System.exit(-1);
                return;
            }

            if (table.getCols() == null || table.getCols().isEmpty()) {
                iterator.remove(); // 移除服务器无用的表
            }
        }
    }

    private void parse(ExcelFile excelFile) throws ExcelParseException {
        System.out.println("开始解析配置表:" + excelFile + " ...");
        long t1 = System.currentTimeMillis();

        try {
            FileInputStream in = new FileInputStream(excelFile.getPath());
            excelFile.setByteSize(in.available());
            XSSFWorkbook wb = new XSSFWorkbook(in);
            if (wb.getNumberOfSheets() < 1) {
                throw new ExcelParseException("工作表不存在");
            }
            if (excelFile.getName().contains("global")) {
                readGlobalExcel(excelFile, wb.getSheetAt(0));
            } else {
                readExcelSheet(excelFile, wb.getSheetAt(0));
            }
        } catch (IOException e) {
            throw new ExcelParseException(e);
        }

        long diff = System.currentTimeMillis() - t1;
        String info = "解析配置表 [" + excelFile.getName() + "] 成功，耗时:" + diff + "毫秒";
        (diff <= 100 ? System.out : System.err).println(info);
    }

    private void readGlobalExcel(ExcelFile table, XSSFSheet sheet) throws ExcelParseException {
        int rows = sheet.getLastRowNum();
        if (rows < 1) {
            throw new ExcelParseException("工作表行数格式错误，不得低于1行");
        }

        table.setRow(1);
        table.setAllRow(rows);

        int col = 0;
        curColNameHolder.set("E");
        for (int i = 1; i <= rows; i++) {
            XSSFRow xssfRow = sheet.getRow(i);
            if (xssfRow == null) {
                continue;
            }

            String desc = readValue(xssfRow, 0);
            String name = readValue(xssfRow, 1);
            String type = readValue(xssfRow, 2);
            String exportFlag = readValue(xssfRow, 3);
            if (name.isEmpty() || !exportFlag.toLowerCase().contains("s")) {
                continue;
            }

            col++;
            table.setCol(col);
            table.setAllCol(col);

            // 拼装字段信息
            ExcelCol excelCol = new ExcelCol();
            excelCol.setCol(col);
            excelCol.setDescT("");
            excelCol.setDesc(desc);
            excelCol.setName(name);

            int row = i + 1;
            curRowNumHolder.set(row);
            curColNameHolder.set("C" + row);
            dealType(type, excelCol);
            table.getCols().add(excelCol);

            // 处理数据
            String value = readValue(xssfRow, 4);
            dealData(excelCol, value, 0);
        }
    }

    private void readExcelSheet(ExcelFile table, XSSFSheet sheet) {
        int rows = sheet.getLastRowNum() + 1;
        table.setAllRow(rows);
        if (rows < BASE_ROW) {
            throw new ExcelParseException("工作表行数格式错误，不得低于" + BASE_ROW + "行，当前[ " + rows + " ]行");
        }

        XSSFRow rowFlag = sheet.getRow(4); // 导给服务器标识
        int cols = rowFlag.getLastCellNum();
        table.setAllCol(cols);

        // 检查有效列
        List<Integer> colList = new ArrayList<>();
        for (int i = 0; i <= cols; i++) {
            String exportFlag = readValue(rowFlag, i);
            if (exportFlag.toLowerCase().contains("s")) {
                colList.add(i);
            }
        }
        if (colList.isEmpty() || !colList.contains(0)) {
            System.out.println(">>> 跳过非服务器所用配置表:" + table);
            return;
        }

        // 检查第一列A5开始之后的列格子
        for (int k = BASE_ROW; k < rows; k++) {
            XSSFRow tmp = sheet.getRow(k);
            if (readValue(tmp, 0).isEmpty()) {
                rows = k;
                break;
            }
        }

        table.setCol(colList.size());
        table.setRow(rows - BASE_ROW);

        XSSFRow rowDescT = sheet.getRow(0);
        XSSFRow rowDesc = sheet.getRow(1);
        XSSFRow rowName = sheet.getRow(2);
        XSSFRow rowType = sheet.getRow(3);
        for (int col : colList) {
            ExcelCol excelCol = new ExcelCol();
            excelCol.setCol(col);
            excelCol.setDescT(readValue(rowDescT, col));
            excelCol.setDesc(readValue(rowDesc, col));
            excelCol.setName(readValue(rowName, col));

            String type = readValue(rowType, col);
            if (type.isEmpty()) {
                throw new ExcelParseException("第[" + excelColName(col) + "4]单元格，字段类型解析错误！");
            }
            String curCol = excelColName(col);
            curColNameHolder.set(curCol);
            dealType(type, excelCol);
            table.getCols().add(excelCol);

            // 读数据
            for (int i = BASE_ROW; i < rows; i++) {
                XSSFRow tmp = sheet.getRow(i);
                String value = readValue(tmp, col);
                curRowNumHolder.set(i + 1);

                dealData(excelCol, value, i - BASE_ROW);
            }
        }
    }

    private void dealData(ExcelCol excelCol, String value, int index) {
        if (excelCol.getWei() == 0) {
            // 基础类型
            List<String> v = new ArrayList<>();
            v.add(getValue(value, excelCol.getFields().get(0).getType(), excelCol));
            wei(excelCol, index, v);
        }

        if (excelCol.getWei() == 1) {
            // 基础类型数组
            if (value.isEmpty()) {
                excelCol.getValues().add(new ArrayList<>());
            } else {
                String[] ds = value.split(",");
                for (String s : ds) {
                    List<String> v = new ArrayList<>();
                    v.add(getValue(s, excelCol.getFields().get(0).getType(), excelCol));
                    wei(excelCol, index, v);
                }
            }
        }

        if (excelCol.getWei() == 2) {
            // 对象
            List<String> v = new ArrayList<>();
            if (value.isEmpty()) {
                // 全部赋默认值
                for (Field field : excelCol.getFields()) {
                    v.add(getValue("", field.getType(), excelCol));
                }
            } else {
                String[] ds = value.split("~");
                if (ds.length != excelCol.getFields().size()) {
                    throw new ExcelParseException("第[" + getCurCell() + "]单元格，对象字段不匹配！");
                }
                for (int j = 0; j < ds.length; j++) {
                    v.add(getValue(ds[j], excelCol.getFields().get(j).getType(), excelCol));
                }
            }
            wei(excelCol, index, v);
        }

        if (excelCol.getWei() == 3) {
            // 对象数组
            if (value.isEmpty()) {
                excelCol.getValues().add(new ArrayList<>());
            } else {
                String[] ds = value.split("\\|");
                for (String d : ds) {
                    String[] ds2 = d.split(",");
                    if (ds2.length != excelCol.getFields().size()) {
                        throw new ExcelParseException("第[" + getCurCell() + "]单元格，数组对象字段不匹配！");
                    }
                    List<String> v = new ArrayList<>();
                    for (int j = 0; j < ds2.length; j++) {
                        v.add(getValue(ds2[j], excelCol.getFields().get(j).getType(), excelCol));
                    }
                    wei(excelCol, index, v);
                }
            }
        }
    }

    private void wei(ExcelCol excelCol, int index, List<String> list) {
        List<List<String>> lists;
        if (excelCol.getValues().size() < index + 1) {
            lists = new ArrayList<>();
            excelCol.getValues().add(lists);
        } else {
            lists = excelCol.getValues().get(index);
        }

        lists.add(list);
    }

    private String getValue(String value, String type, ExcelCol excelCol) {
        try {
            return switch (type) {
                case "int" -> "".equals(value) ? "0" : String.valueOf(Integer.parseInt(value));
                case "long" -> "".equals(value) ? "0" : String.valueOf(Long.parseLong(value));
                case "float" -> Objects.equals(value, "") ? "0" : String.valueOf(Float.parseFloat(value));
                case "String" -> value;
                default -> null;
            };
        } catch (Exception e) {
            throw new ExcelParseException("第[" + getCurCell() + "]单元格，数据解析失败！数据:" + value + " 类型:" + excelCol.getType());
        }
    }

    private String readValue(XSSFRow row, int col) {
        if (row == null) {
            return "";
        }

        XSSFCell xssfCell = row.getCell(col);
        if (xssfCell == null) {
            return "";
        }

        String value;
        switch (xssfCell.getCellType()) {
            case STRING -> value = xssfCell.getStringCellValue();
            case FORMULA -> value = xssfCell.getCTCell().getV();
            case NUMERIC -> value = numberFormat.format(xssfCell.getNumericCellValue());
            default -> {
                xssfCell.setCellType(CellType.STRING);
                value = xssfCell.toString().trim();
            }
        }
        return value.equalsIgnoreCase("null") ? "" : value.trim();
    }

    private String getCurCell() {
        return curColNameHolder.get() + curRowNumHolder.get();
    }

    private void checkSpecialStruct(ExcelCol excelCol) {
        if (excelCol.getFields().size() != 2) {
            return;
        }

        String keyType = excelCol.getFields().get(0).getType();
        String valueType = excelCol.getFields().get(1).getType();
        if (!keyType.equals("int") || !supportBaseTypeSet.contains(valueType)) {
            return;
        }

        StringBuilder typeDesc = new StringBuilder();
        for (Field field : excelCol.getFields()) {
            typeDesc.append(field.getName()).append(" - ");
        }
        typeDesc.setLength(typeDesc.length() - 3);
        typeDesc.append(" -- ").append(excelCol.getDesc());

        // 满足条件!
        excelCol.setSpecialType(1);
        // 将原来的类型写入注释
        excelCol.setDesc(typeDesc.toString());

        switch (valueType) {
            case "int":
                if (excelCol.getWei() == 3) {
                    excelCol.setType("List<Entry.Int2IntVal>");
                } else {
                    excelCol.setType("Int2IntVal");
                }
                excelCol.setTypeClass("Int2IntVal");
                break;
            case "long":
                if (excelCol.getWei() == 3) {
                    excelCol.setType("List<Entry.Int2LongVal>");
                } else {
                    excelCol.setType("Entry.Int2LongVal");
                }
                excelCol.setTypeClass("Entry.Int2LongVal");
                break;
            case "float":
                if (excelCol.getWei() == 3) {
                    excelCol.setType("List<Entry.Int2FloatVal>");
                } else {
                    excelCol.setType("Entry.Int2FloatVal");
                }
                excelCol.setTypeClass("Entry.Int2FloatVal");
                break;
        }

        excelCol.getFields().get(0).setName("k");
        excelCol.getFields().get(1).setName("v");
    }

    private void dealType(String type, ExcelCol excelCol) {
        if (type.startsWith("[]{")) {
            // 数组对象
            excelCol.setWei(3);
            excelCol.setType("List<S" + firstCapital(excelCol.getName()) + ">");
            excelCol.setTypeClass("S" + firstCapital(excelCol.getName()));
            String type2 = type.substring(3, type.length() - 1);
            dealWei(type2, excelCol);
            checkSpecialStruct(excelCol);
        } else if (type.startsWith("{")) {
            // 对象
            excelCol.setWei(2);
            excelCol.setType("S" + firstCapital(excelCol.getName()));
            excelCol.setTypeClass(excelCol.getType());
            String type2 = type.substring(1, type.length() - 1);
            dealWei(type2, excelCol);
            checkSpecialStruct(excelCol);
        } else if (type.startsWith("[]")) {
            // 数组
            excelCol.setWei(1);
            String type2 = convertType(type.substring(2));
            if (type2 == null) {
                throw new ExcelParseException("字段类型解析失败，列:" + curColNameHolder.get());
            }
            excelCol.setType(convertListType(type2));
            excelCol.getFields().add(new Field(type2, excelCol.getName()));
        } else {
            // 基本结构
            String type2 = convertType(type);
            if (type2 == null) {
                throw new ExcelParseException("字段类型解析失败，列:" + curColNameHolder.get());
            }
            excelCol.setWei(0);
            excelCol.setType(type2);
            excelCol.getFields().add(new Field(type2, excelCol.getName()));
        }
    }

    private void dealWei(String type, ExcelCol excelCol) {
        String[] types = type.split(";");
        for (String t : types) {
            if (t.trim().isEmpty()) {
                continue;
            }
            String[] cs = t.trim().split(" ");
            if (cs.length != 2) {
                throw new ExcelParseException("字段类型解析失败，列:" + curColNameHolder.get());
            }

            String type3 = convertType(cs[0]);
            if (type3 == null) {
                throw new ExcelParseException("字段类型解析失败，列:" + curColNameHolder.get());
            }

            excelCol.getFields().add(new Field(type3, cs[1]));
        }
    }

    /**
     * 基本类型
     */
    private String convertType(String type) {
        return switch (type) {
            case "int", "int32" -> "int";
            case "int64" -> "long";
            case "float" -> "float";
            case "string" -> "String";
            default -> null;
        };
    }

    private String convertListType(String type) {
        return switch (type) {
            case "int" -> "List<Integer>";
            case "long" -> "List<Long>";
            case "float" -> "List<Float>";
            case "String" -> "List<String>";
            default -> null;
        };
    }
}
