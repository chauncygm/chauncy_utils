package cn.chauncy.export;

import cn.chauncy.ExportOption;
import cn.chauncy.struct.SheetInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import static cn.chauncy.ExcelUtil.firstCapital;
import static freemarker.template.Configuration.VERSION_2_3_34;

public class ServerExporter {
    public static final ServerExporter INSTANCE = new ServerExporter();

    private final Configuration cfg;

    private ServerExporter() {
        cfg = new Configuration(VERSION_2_3_34);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setClassForTemplateLoading(ServerExporter.class, "/ftl/server");
    }

    public void export(Map<Integer, SheetInfo> sheetMap, ExportOption option)  throws IOException, TemplateException {
        for (SheetInfo sheetInfo : sheetMap.values()) {
            writeClass(sheetInfo, option.getOutputPath());
//            writeJson(sheetInfo, option.getOutputPath());
        }
        writeDefineClass(sheetMap.values(), option.getOutputPath());
    }

    public void writeClass(SheetInfo sheetInfo, Path path) throws IOException, TemplateException {
        String ftlName = "class.ftl";
        Path classFilePath = Path.of("bean1", firstCapital(sheetInfo.getSheetName()) + ".java");
        if ("tips".equals(sheetInfo.getSheetName())) {
            ftlName = "tips.ftl";
            classFilePath = Path.of("CfgTips.java");
        }
        File file = Path.of(path.toString(), classFilePath.toString()).toFile();
        writeTemplate(file, ftlName, sheetInfo);
    }

    public void writeDefineClass(Collection<SheetInfo> sheetInfos, Path path) throws IOException, TemplateException {
        File file = Path.of(path.toString(), "CfgDefine.java").toFile();
        writeTemplate(file, "define.ftl", sheetInfos);
    }

    public void writeJson(SheetInfo sheetInfo, Path path) throws IOException, TemplateException {
        File jsonFile = Path.of(path.toString(), sheetInfo.getSheetName() + ".json").toFile();
        writeTemplate(jsonFile, "json.ftl", sheetInfo);
    }

    private void writeTemplate(File file, String ftlName, Object data) throws IOException, TemplateException {
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
        Map<String, Object> dataModel = Map.of("data", data);
        try (FileWriter fileWriter = new FileWriter(file)){
            Template template = cfg.getTemplate(ftlName);
            template.process(dataModel, fileWriter);
        }
    }

}
