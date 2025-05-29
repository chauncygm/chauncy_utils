package cn.chauncy;

import cn.chauncy.struct.ExcelCol;
import cn.chauncy.struct.Field;
import cn.chauncy.struct.TableFile;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;


public class ReadExcel {
    public static final ReadExcel INSTANCE = new ReadExcel();
    private static final NumberFormat numberFormat;

    static {
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(6);
    }

    public void read(List<TableFile> list) {
        try {
            list.sort(Comparator.comparingInt(TableFile::getId));
            Iterator<TableFile> iterator = list.iterator();
            while (iterator.hasNext()) {
                TableFile table = iterator.next();
                String ret = read(table);
                if (ret != null) {
                    System.err.println("读取表数据失败{" + ret + "}，中止:" + table);
                    System.exit(-1);
                    return;
                }

                if (table.getCols() == null || table.getCols().isEmpty()) {
                    iterator.remove(); // 移除服务器无用的表
                }
            }
        } catch (IOException e) {
            System.err.println("读取excel失败，退出");
            System.exit(-1);
        }
    }

    private String read(TableFile table) throws IOException {
        System.out.println("开始解析配置表:" + table.toString2() + " ...");
        long t1 = System.currentTimeMillis();
        FileInputStream in = new FileInputStream(table.getPath());
        table.setSize(in.available());
        ZipSecureFile.setMinInflateRatio(-1.0d);
        XSSFWorkbook wb = new XSSFWorkbook(in);
        int sheetNumber = wb.getNumberOfSheets(); //获得工作表数量
        if (sheetNumber < 1) {
            return "工作表不存在";
        }
        String ret;
        if (table.getId() != 98)
            ret = readExcelSheet(table, wb.getSheetAt(0));
        else
            ret = readGlobalExcel(table, wb.getSheetAt(0));
        if (ret != null)
            return ret;

        long t2 = System.currentTimeMillis();
        long diff = t2 - t1;
        if (diff <= 100)
            System.out.println("解析配置表 [" + table.getName() + "] 成功，耗时:" + diff + "毫秒");
        else
            System.err.println("解析配置表 [" + table.getName() + "] 成功，耗时:" + diff + "毫秒");
        return null;
    }

    private String readGlobalExcel(TableFile table, XSSFSheet sheet) {
        int rows = sheet.getLastRowNum();
        table.setAllRow(rows);
        if (rows < 1) {
            return "工作表行数格式错误，不得低于1行";
        }

        table.setRow(1);
        table.setCols(new ArrayList<>());

        XSSFCell xssfCell;
        XSSFRow xssfRow;
        int col = 0;
        String value, ret;
        for (int i = 1; i <= rows; i++) {
            xssfRow = sheet.getRow(i);
            if (xssfRow == null) continue;
            xssfCell = xssfRow.getCell(3); // 导给服务器标识
            if (xssfCell == null || !xssfCell.toString().trim().toLowerCase().contains("s")) continue;

            xssfCell = xssfRow.getCell(1);
            if (xssfCell.toString().trim().isEmpty())
                continue;

            col++;

            table.setCol(col);
            table.setAllCol(col);

            // 拼装字段信息
            ExcelCol excelCol = new ExcelCol();
            excelCol.setCol(col);
            excelCol.setDescT(xssfRow.getCell(0).toString());
            excelCol.setDesc(excelCol.getDescT());
            excelCol.setName(xssfRow.getCell(1).toString().trim());

            String type = xssfRow.getCell(2).toString().trim();
            dealType(type, excelCol, "C" + (i + 1));
            table.getCols().add(excelCol);

            // 处理数据
            xssfCell = xssfRow.getCell(4);
            value = readValue(xssfCell);
            ret = dealData(excelCol, value, 0, "E", i + 1);
            if (ret != null)
                return ret;
        }
        return null;
    }

    private String readExcelSheet(TableFile table, XSSFSheet sheet) {
        int BASE_ROW = 5;
        int rows = sheet.getLastRowNum() + 1;
        table.setAllRow(rows);
        if (rows < BASE_ROW) {
            return "工作表行数格式错误，不得低于5行，目前仅[ " + rows + " ]行";
        }

        // 检查有效列
        List<Integer> colList = new ArrayList<>();
        XSSFRow rowFlag = sheet.getRow(4); // 导给服务器标识
        XSSFCell xssfCell;
        int cols = rowFlag.getLastCellNum();
        table.setAllCol(cols);
        for (int i = 0; i <= cols; i++) {
            xssfCell = rowFlag.getCell(i);
            if (xssfCell == null || !xssfCell.toString().trim().toLowerCase().contains("s")) continue;
            colList.add(i);
        }

        if (colList.isEmpty() || !colList.contains(0)) {
            System.out.println(">>> 跳过非服务器所用配置表:" + table.toString2());
            return null;
        }

        // 检查第一列A5开始之后的列格子
        XSSFRow tmp;
        for (int k = BASE_ROW; k < rows; k++) {
            tmp = sheet.getRow(k);
            if (null == tmp
                    || null == tmp.getCell(0)
                    || tmp.getCell(0).toString().trim().isEmpty()) {
//                return "第[A" + (k + 1) + "]单元格，Key值为空！";
                rows = k;
                break;
            }
        }

        table.setCol(colList.size());
        table.setRow(rows - BASE_ROW);

        table.setCols(new ArrayList<>(colList.size()));

        XSSFRow rowDescT = sheet.getRow(0);
        XSSFRow rowDesc = sheet.getRow(1);
        XSSFRow rowName = sheet.getRow(2);
        XSSFRow rowType = sheet.getRow(3);
        for (int col : colList) {
            ExcelCol excelCol = new ExcelCol();
            excelCol.setCol(col);
            excelCol.setDescT(readValue(rowDescT.getCell(col)));
            excelCol.setDesc(readValue(rowDesc.getCell(col)));
            excelCol.setName(readValue(rowName.getCell(col)));

//            rowType.getCell(col).
            String type = readValue(rowType.getCell(col));
            if (type.isEmpty()) {
                return "第[" + excelColName(col) + "4]单元格，字段类型解析错误！";
            }
            dealType(type, excelCol, excelCol.getName());
            table.getCols().add(excelCol);

            // 读数据
            String value, ret;
            for (int i = BASE_ROW; i < rows; i++) {
                tmp = sheet.getRow(i);
                if (tmp != null) {
                    value = readValue(tmp.getCell(col));
                } else value = "";

                ret = dealData(excelCol, value, i - BASE_ROW, excelColName(col), i + 1);
                if (ret != null)
                    return ret;
            }
        }

        return null;
    }

    private String excelColName(int col) {
        if (col < 26)
            return String.valueOf((char) (col + 65));
        int c = col / 26 - 1;
        String h = String.valueOf((char) (c + 65));
        String y = String.valueOf((char) (col % 26 + 65));
        return h + y;
    }

    private String dealData(ExcelCol excelCol, String value, int index, String col, int row) {
        if (excelCol.getWei() == 0) {
            // 基础类型
            List<String> v = new ArrayList<>();
            v.add(getValue(value, excelCol.getFields().get(0).getType(), excelCol, col, row));
            wei(excelCol, index, v);
        } else if (excelCol.getWei() == 1) {
            // 基础类型数组
            if (value.isEmpty()) {
                excelCol.getValues().add(new ArrayList<>());
            } else {
                String[] ds = value.split("\\|");
                for (String s : ds) {
                    List<String> v = new ArrayList<>();
                    v.add(getValue(s, excelCol.getFields().get(0).getType(), excelCol, col, row));
                    wei(excelCol, index, v);
                }
            }
        } else if (excelCol.getWei() == 2) {
            // 对象
            List<String> v = new ArrayList<>();
            if (value.isEmpty()) {
                // 全部赋默认值
                for (Field field : excelCol.getFields()) {
                    v.add(getValue("", field.getType(), excelCol, col, row));
                }
            } else {
                String[] ds = value.split("~");
                if (ds.length != excelCol.getFields().size()) {
                    return "第[" + col + row + "]单元格，对象字段不匹配！";
                }
                for (int j = 0; j < ds.length; j++) {
                    v.add(getValue(ds[j], excelCol.getFields().get(j).getType(), excelCol, col, row));
                }
            }
            wei(excelCol, index, v);
        } else {
            // 对象数组
            if (value.isEmpty()) {
                excelCol.getValues().add(new ArrayList<>());
            } else {
                String[] ds = value.split("\\|");
                for (String d : ds) {
                    String[] ds2 = d.split("~");
                    if (ds2.length != excelCol.getFields().size()) {
                        return "第[" + col + row + "]单元格，数组对象字段不匹配！";
                    }
                    List<String> v = new ArrayList<>();
                    for (int j = 0; j < ds2.length; j++) {
                        v.add(getValue(ds2[j], excelCol.getFields().get(j).getType(), excelCol, col, row));
                    }
                    wei(excelCol, index, v);
                }
            }
        }
        return null;
    }

    private void wei(ExcelCol excelCol, int index, List<String> list) {
        List<List<String>> lists;
        if (excelCol.getValues().size() < index + 1) {
            lists = new ArrayList<>();
            excelCol.getValues().add(lists);
        } else
            lists = excelCol.getValues().get(index);

        lists.add(list);
    }

    private String getValue(String value, String type, ExcelCol excelCol, String col, int row) {
        try {
            return switch (type) {
                case "int" ->
//                    if (value.endsWith(".0")) value = value.substring(0, value.length() - 2);
//                    value = value.replace(value, ".0");
                        Objects.equals(value, "") ? "0" : String.valueOf(Integer.parseInt(value));
                case "long" ->
//                    if (value.endsWith(".0")) value = value.substring(0, value.length() - 2);
                        Objects.equals(value, "") ? "0" : String.valueOf(Long.parseLong(value));
                case "float", "double" -> Objects.equals(value, "") ? "0" : String.valueOf(Float.parseFloat(value));
                case "String" -> value;
                default -> null;
            };
        } catch (Exception e) {
            System.err.println("第[" + col + row + "]单元格，数据解析失败！数据:" + value + " 类型:" + excelCol.getType());
            System.exit(-1);
            return null;
        }
    }

    private String readValue(XSSFCell xssfCell) {
        if (xssfCell == null)
            return "";

        CellType cellType = xssfCell.getCellType();
        String value = "";
        try {
            if (cellType == CellType.STRING) {
                value = xssfCell.getStringCellValue();
            } else if (cellType == CellType.FORMULA) {
                // 公式
//                value = String.valueOf(xssfCell.getNumericCellValue());
                value = xssfCell.getCTCell().getV();
            } else if (cellType == CellType.NUMERIC) {
                double d = xssfCell.getNumericCellValue();
                value = numberFormat.format(d);
//                value = String.valueOf(xssfCell.getNumericCellValue());
            } else {
                xssfCell.setCellType(CellType.STRING);
                value = xssfCell.toString().trim();
            }
        } catch (Exception e) {
            System.err.println("设置类型出错:" + cellType);
        }
        if (value.equalsIgnoreCase("null"))
            value = "";
        return value;
    }

    private void checkSpecialStruct(ExcelCol excelCol) {
        if (excelCol.getFields().size() != 2)
            return;

        if (!excelCol.getFields().get(0).getType().equals("int"))
            return;

        switch (excelCol.getFields().get(1).getType()) {
            case "int":
            case "long":
            case "float":
                break;
            default:return;
        }

        StringBuilder types = new StringBuilder();
        for (Field field : excelCol.getFields()) {
//            if (!field.getType().equals("int"))
//                return;
            types.append(field.getName()).append(" - ");
        }

        // 满足条件!
        excelCol.setSpecialType(1);

        types.append(" -- ").append(excelCol.getDesc());

        // 将原来的类型写入注释
        excelCol.setDesc(types.toString());

        switch (excelCol.getFields().get(1).getType()) {
            case "int":
                if (excelCol.getWei() == 3) {
                    excelCol.setType("List<IntKeyValue>");
                } else {
                    excelCol.setType("IntKeyValue");
                }
                excelCol.setTypeClass("IntKeyValue");
                break;
            case "long":
                if (excelCol.getWei() == 3) {
                    excelCol.setType("List<IntKeyLongVal>");
                } else {
                    excelCol.setType("IntKeyLongVal");
                }
                excelCol.setTypeClass("IntKeyLongVal");
                break;
            case "float":
                if (excelCol.getWei() == 3) {
                    excelCol.setType("List<IntKeyFloatVal>");
                } else {
                    excelCol.setType("IntKeyFloatVal");
                }
                excelCol.setTypeClass("IntKeyFloatVal");
                break;
        }

        excelCol.getFields().get(0).setName("k");
        excelCol.getFields().get(1).setName("v");
    }

    private void dealType(String type, ExcelCol excelCol, String col) {
        if (type.startsWith("[]{")) {
            // 数组对象
            excelCol.setWei(3);
            excelCol.setType("List<S" + firstCapital(excelCol.getName()) + ">");
            excelCol.setTypeClass("S" + firstCapital(excelCol.getName()));
            String type2 = type.substring(3, type.length() - 1);
            dealWei(type2, excelCol, col);
            checkSpecialStruct(excelCol);
        } else if (type.startsWith("{")) {
            // 对象
            excelCol.setWei(2);
            excelCol.setType("S" + firstCapital(excelCol.getName()));
            excelCol.setTypeClass(excelCol.getType());
            String type2 = type.substring(1, type.length() - 1);
            dealWei(type2, excelCol, col);
            checkSpecialStruct(excelCol);
        } else if (type.startsWith("[]")) {
            // 数组
            excelCol.setWei(1);
            String type2 = convertType(type.substring(2));
            if (type2 == null) {
                System.err.println("字段类型解析失败，列:" + col);
                System.exit(-1);
                return;
            }
            excelCol.setType(convertListType(type2));
            excelCol.getFields().add(new Field(type2, excelCol.getName()));
        } else {
            // 基本结构
            String type2 = convertType(type);
            if (type2 == null) {
                System.err.println("字段类型解析失败，列:" + col);
                System.exit(-1);
                return;
            }
            excelCol.setType(type2);
            excelCol.getFields().add(new Field(type2, excelCol.getName()));
        }
    }

    private void dealWei(String type, ExcelCol excelCol, String col) {
        String[] types = type.split(";");
        for (String t : types) {
            t = t.trim();
            if (t.isEmpty()) continue;
            String[] cs = t.split(" ");
            if (cs.length != 2) {
                System.err.println("字段类型解析失败，列:" + col);
                System.exit(-1);
                return;
            }

            String type3 = convertType(cs[0]);
            if (type3 == null) {
                System.err.println("字段类型解析失败，列:" + col);
                System.exit(-1);
                return;
            }

            excelCol.getFields().add(new Field(type3, cs[1]));
        }
    }

    public String firstCapital(String string) {
        char c = string.charAt(0);
        if (c >= 'a' && c <= 'z')
            c -= 32;
        return c + string.substring(1);
    }

    /**
     * 基本类型
     */
    private String convertType(String type) {
        return switch (type) {
            case "int32" -> "int";
            case "int64" -> "long";
            case "float" -> "float";
            case "double" -> "double";
            case "string" -> "String";
            default -> null;
        };
    }

    private String convertListType(String type) {
        return switch (type) {
            case "int" -> "List<Integer>";
            case "long" -> "List<Long>";
            case "float" -> "List<Float>";
            case "double" -> "List<>Double";
            case "String" -> "List<String>";
            default -> null;
        };
    }
}
