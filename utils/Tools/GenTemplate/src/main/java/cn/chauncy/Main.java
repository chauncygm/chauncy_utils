package cn.chauncy;

import cn.chauncy.struct.ExcelFile;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class Main {


    public static void main(String[] args) {
        ExcelExportConfig.loadConfig();

        List<Integer> exportIds = getInput();

        long t1 = System.currentTimeMillis();
        Path cfgPath = ExcelExportConfig.getExcelPath();
        List<ExcelFile> list = ExcelReader.INSTANCE.scanExcelDir(cfgPath.toFile(), exportIds);
        ExcelReader.INSTANCE.parse(list);
        long t2 = System.currentTimeMillis();
        System.out.println("=== 读取配置成功, 数量: " + list.size() + " 总耗时: " + (t2 - t1) + "ms！ ===");



        System.out.println("正在准备写入数据...");
        TemplateGenerator.INSTANCE.write(list, exportIds.isEmpty());
        t2 = System.currentTimeMillis();
        System.out.println("=== 结束！数量: " + list.size() + ", 总耗时: " + (t2 - t1) + "毫秒！ ===");
    }

    private static List<Integer> getInput() {
        System.out.println("输入要导出的表ID(导全表直接回车，多个表以空格分隔): ");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            List<Integer> list = new ArrayList<>();
            String str = br.readLine().trim();
            if (str.isEmpty() || str.equals("0")) {
                return list;
            }

            String[] ids = str.split(" ");
            for (String id : ids) {
                list.add(Integer.parseInt(id));
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
