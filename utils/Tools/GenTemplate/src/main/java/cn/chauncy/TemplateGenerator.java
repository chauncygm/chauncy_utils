package cn.chauncy;

import cn.chauncy.struct.ExcelFile;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static cn.chauncy.ExcelUtil.firstCapital;
import static freemarker.template.Configuration.VERSION_2_3_34;

public class TemplateGenerator {

    public static final TemplateGenerator INSTANCE = new TemplateGenerator();

    private static final String path = ExcelExportConfig.getOutputPath().toString() + "\\";
    private static final String dataPath = ExcelExportConfig.getExportPath().toString() + "\\";

    /**
     * 模板
     */
    private Configuration cfg = null;

    private TemplateGenerator() {
        // 模板
        try {
            cfg = new Configuration(VERSION_2_3_34);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setClassForTemplateLoading(getClass(), "/ftl");
        } catch (Exception e) {
            System.err.println("读取模板失败");
            System.exit(-1);
        }
    }

    public void write(List<ExcelFile> list, boolean isAll) {
        int count = 0;
        if (isAll) {
            clearDir(ExcelExportConfig.getExportPath().toString());
            clearDir(ExcelExportConfig.getOutputPath().toString() + "\\bean");
        }

        try {
            Iterator<ExcelFile> iterator = list.iterator();
            while (iterator.hasNext()) {
                ExcelFile excelFile = iterator.next();
                count++;
                long t1 = System.currentTimeMillis();
                if (excelFile.getName().contains("tips")) {
                    writeTips(excelFile);
                    iterator.remove();
                } else {
                    writeClass(excelFile);
                    writeJson(excelFile);
                }
                long t2 = System.currentTimeMillis();
                System.out.println("写入配置数据 [" + excelFile.getName() + "] 成功，耗时:" + (t2 - t1) + "毫秒");
            }

            if (isAll)
                writeDefine(list);
        } catch (Exception e) {
            System.err.println("生成配置文件失败");
        }
        System.out.println("写入配置数据数量：" + count);
    }

    /**
     * 清理所有配置与结构
     */
    private void clearDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            return;
        }

        for (String name : Objects.requireNonNull(file.list())) {
            File temp = new File(dir, name);
            if (temp.isDirectory()) {
                clearDir(temp.getAbsolutePath());//递归调用，删除目录里的内容
                //noinspection ResultOfMethodCallIgnored
                temp.delete();//删除空目录
            } else {
                if (!temp.delete()) {//直接删除文件
                    System.err.println("Failed to delete " + name);
                }
            }
        }
    }

    private void writeClass(ExcelFile excelFile) throws IOException, TemplateException {
        Map<String, Object> root = Map.of("data", excelFile);
        writeTemplateToFile(path + "bean\\" + "Cfg" + firstCapital(excelFile.getName()) + ".java", "class.ftl", root);
    }

    private void writeJson(ExcelFile excelFile) throws IOException, TemplateException {
        Map<String, Object> root = Map.of("data", excelFile);
        writeTemplateToFile(dataPath + excelFile.getName() + ".json", "json.ftl", root);
    }

    private void writeTips(ExcelFile excelFile) throws IOException, TemplateException {
        Map<String, Object> root = Map.of("data", excelFile);
        writeTemplateToFile(path + "CfgTips.java", "tips.ftl", root);
    }

    private void writeDefine(List<ExcelFile> list) throws IOException, TemplateException {
        Map<String, Object> root = Map.of("datalist", list);
        writeTemplateToFile(path + "CfgDefine.java", "define.ftl", root);
    }

    private void writeTemplateToFile(String filePath, String templateName, Map<String, Object> dataModel) throws IOException, TemplateException {
        File file = new File(filePath);
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }

        try (FileWriter fileWriter = new FileWriter(file)){
            Template template = cfg.getTemplate(templateName);
            template.process(dataModel, fileWriter);
        }
    }
}
