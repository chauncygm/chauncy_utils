package cn.chauncy.export;

import cn.chauncy.option.ExportOption;
import cn.chauncy.struct.SheetInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static cn.chauncy.util.ExcelUtil.firstCapital;
import static freemarker.template.Configuration.VERSION_2_3_34;

public class ServerExporter {

    private static final Logger logger = LoggerFactory.getLogger(ServerExporter.class);

    public static final ServerExporter INSTANCE = new ServerExporter();

    private final Configuration cfg;

    private ServerExporter() {
        cfg = new Configuration(VERSION_2_3_34);
        cfg.setClassForTemplateLoading(ServerExporter.class, "/ftl/server");
        cfg.setLocale(Locale.CHINA);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setNumberFormat("0");
    }

    public void export(Map<Integer, SheetInfo> sheetMap, ExportOption option) {
        logger.info("开始导出代码和json配置文件... \n输出代码路径: {}\n输出json文件路径：{}",
                option.getClassOutputPath().toFile().getAbsolutePath(),
                option.getJsonOutputPath().toFile().getAbsolutePath());
        long start = System.currentTimeMillis();
        for (SheetInfo sheetInfo : sheetMap.values()) {
            logger.info("开始导出代码和json文件：{}", sheetInfo.getSheetName());
            if (sheetInfo.getSheetName().equals("tips")) {
                writeTips(sheetInfo, option);
                continue;
            }
            writeClass(sheetInfo, option);
            writeJson(sheetInfo, option);
        }
        if (option.getExportIds().isEmpty()) {
            writeDefineClass(sheetMap.values(), option);
        }
        logger.info("配置生成代码和json配置文件导出完毕！ size: [{}], use time: [{}]ms", sheetMap.size(), System.currentTimeMillis() - start);
    }

    public void writeClass(SheetInfo sheetInfo, ExportOption option) {
        File file = buildOutputPath(option, "bean", "Cfg" + firstCapital(sheetInfo.getSheetName()) + ".java").toFile();
        Map<String, Object> dataModel = Map.of("data", sheetInfo, "package", option.getClassOutPackage());
        writeTemplate(file, "class.ftl", dataModel);
    }

    public void writeTips(SheetInfo sheetInfo, ExportOption option) {
        File file = buildOutputPath(option, "Cfg" + firstCapital(sheetInfo.getSheetName()) + ".java").toFile();
        Map<String, Object> dataModel = Map.of("data", sheetInfo, "package", option.getClassOutPackage());
        writeTemplate(file, "tips.ftl", dataModel);
    }

    public void writeDefineClass(Collection<SheetInfo> sheetInfos, ExportOption option) {
        File file = buildOutputPath(option, "CfgDefine.java").toFile();
        Map<String, Object> dataModel = Map.of("data", sheetInfos, "package", option.getClassOutPackage());
        writeTemplate(file, "define.ftl", dataModel);
    }

    public void writeJson(SheetInfo sheetInfo, ExportOption option) {
        File jsonFile = option.getJsonOutputPath().resolve(sheetInfo.getSheetName() + ".json").toFile();
        Map<String, Object> dataModel = Map.of("data", sheetInfo, "package", option.getClassOutPackage());
        writeTemplate(jsonFile, "json.ftl", dataModel);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeTemplate(File file, String ftlName, Object dataModel) {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (FileWriter fileWriter = new FileWriter(file)){
                Template template = cfg.getTemplate(ftlName);
                template.process(dataModel, fileWriter);
            }
        } catch (IOException | TemplateException e) {
            logger.error("模版生成失败！ {}", file.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
    }

    private Path buildOutputPath(ExportOption option, String... subPaths) {
        Path path = option.getClassOutputPath();
        path = path.resolve("src\\main\\java");
        path = path.resolve(option.getClassOutPackage().replace(".", "\\"));
        for (String sub : subPaths) {
            path = path.resolve(sub);
        }
        return path;
    }

}
