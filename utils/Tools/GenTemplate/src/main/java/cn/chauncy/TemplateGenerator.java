package cn.chauncy;

import cn.chauncy.struct.ExcelFile;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

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

    public int write(List<ExcelFile> list, boolean isAll) {
        int count = 0;

        if (isAll) {
            clearDir(ExcelExportConfig.getExportPath().toString());
            clearDir(ExcelExportConfig.getOutputPath().toString() + "\\bean");
        }

        try {
            for (ExcelFile excelFile : list) {

                count++;
                long t1 = System.currentTimeMillis();
                if (excelFile.getName().contains("tips")) {
                    writeTips(excelFile);
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
        return count;
    }

    /**
     * 清理所有配置与结构
     */
    private void clearDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            return;
        }

        String[] content = file.list();//取得当前目录下所有文件和文件夹
        if (content == null)
            return;

        for (String name : content) {
            File temp = new File(dir, name);
            if (temp.isDirectory()) {//判断是否是目录
                clearDir(temp.getAbsolutePath());//递归调用，删除目录里的内容
                temp.delete();//删除空目录
            } else {
                if (!temp.delete()) {//直接删除文件
                    System.err.println("Failed to delete " + name);
                }
            }
        }
    }

    private void writeClass(ExcelFile excelFile) throws IOException, TemplateException {
        HashMap<String, Object> root = new HashMap<>();
        root.put("tc", excelFile);

        Writer out = new StringWriter();
        Template temp = cfg.getTemplate("class.ftl");
        temp.process(root, out);
        writeFile(path + "bean\\Cfg" + firstCapital(excelFile.getName()) + ".java", out.toString());
        out.close();
    }

    private void writeJson(ExcelFile excelFile) throws IOException, TemplateException {
        HashMap<String, Object> root = new HashMap<>();
        root.put("tc", excelFile);

        Writer out = new StringWriter();
        Template temp = cfg.getTemplate("json.ftl");
        temp.process(root, out);
        writeFile(dataPath + excelFile.getName() + ".json", out.toString());
        out.close();
    }

    private void writeTips(ExcelFile excelFile) throws IOException, TemplateException {
        HashMap<String, Object> root = new HashMap<>();
        root.put("tc", excelFile);

        Writer out = new StringWriter();
        Template temp = cfg.getTemplate("tips.ftl");
        temp.process(root, out);
        writeFile(path + "CfgTips.java", out.toString());
        out.close();
    }

    private void writeDefine(List<ExcelFile> list) throws IOException, TemplateException {
        HashMap<String, Object> root = new HashMap<>();
        root.put("data", list);

        Writer out = new StringWriter();
        Template temp = cfg.getTemplate("define.ftl");
        temp.process(root, out);
        writeFile(path + "CfgDefine.java", out.toString());
        out.close();
    }

    private void writeFile(String filePath, String content) {
        try {
            File file = new File(filePath);
            FileOutputStream fos;
            if (!file.exists()) {
                file.createNewFile();
                fos = new FileOutputStream(file);
            } else {
                fos = new FileOutputStream(file, false);
            }
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writer.write(content);
            writer.flush();
            writer.close();
            fos.close();
        } catch (IOException e) {
            System.err.println("写入文件失败:" + filePath);
        }
    }
}
