package cn.chauncy;

public class ExcelUtil {


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
