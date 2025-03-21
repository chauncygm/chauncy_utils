package com.chauncy.utils;

public class StringUtils {

    /**
     * 下划线转小驼峰（变量/方法命名）
     * @param underlineStr 下划线格式字符串，例：user_name
     * @return 驼峰格式字符串，例：userName
     */
    public static String underlineToCamel(String underlineStr) {
        if (underlineStr == null || underlineStr.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < underlineStr.length(); i++) {
            char c = underlineStr.charAt(i);

            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    sb.append(i == 0 ? Character.toLowerCase(c) : c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 下划线转大驼峰（类名）
     * @param underlineStr 下划线格式字符串，例：user_info
     * @return 大驼峰格式字符串，例：UserInfo
     */
    public static String underlineToPascal(String underlineStr) {
        String camel = underlineToCamel(underlineStr);
        return camel.isEmpty() ? "" :
                Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }
}
