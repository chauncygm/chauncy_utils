package cn.chauncy.util;

import cn.chauncy.ExcelReader;
import cn.chauncy.struct.SheetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ExcelUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);
    private static final StringBuilder sb = new StringBuilder();

    private static final Set<String> excelFileSuffix = Set.of("xls", "xlsx");

    public static boolean readExcel(SheetInfo sheetInfo) {
        try (final ExcelReader reader = new ExcelReader(sheetInfo)) {
            reader.read();
            return true;
        } catch (Exception e) {
            logger.error("读取Excel文件{}出错.", sheetInfo.getSheetName(), e);
            return false;
        }
    }
    public static boolean isExcelFile(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return false;
        }
        String suffix = fileName.substring(index + 1);
        return excelFileSuffix.contains(suffix);
    }

    public static String getExcelName(String fileName) {
        int index = fileName.indexOf(".");
        return fileName.substring(0, index);
    }

    /**
     * 获取Excel列名
     *
     * @param col 列号
     * @return 列名
     */
    public static String excelColName(int col) {
        if (col < 0) {
            throw new IllegalArgumentException("列号不能为负数");
        }

        StringBuilder sb = new StringBuilder();
        do {
            sb.append((char) ('A' + col % 26));
            col = col / 26 - 1;
        } while (col >= 0);

        return sb.reverse().toString();
    }

    /**
     * 首字母大写
     *
     * @param str 输入字符串
     * @return 首字母大写的字符串
     */
    public static String firstCapital(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("输入字符串不能为空");
        }

        char firstChar = str.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            firstChar = Character.toUpperCase(firstChar);
        }
        return firstChar + str.substring(1);
    }
}
