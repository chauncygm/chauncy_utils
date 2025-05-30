package cn.chauncy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ExcelExportConfig {

    private static final Path CONFIG_PATH = Paths.get("./config/config.properties");

    private static Path excelPath;
    private static Path exportPath;

    private static Path outputPath;

    public static void loadConfig() {
        File configFile = CONFIG_PATH.toFile();
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(configFile));
        } catch (IOException e) {
            throw new RuntimeException("configFile not exist");
        }

        excelPath = Path.of(properties.getProperty("excel.path"));
        exportPath = Path.of(properties.getProperty("export.path"));
        outputPath = Paths.get(properties.getProperty("output.path"));
        if (!exportPath.toFile().exists()
                || !exportPath.toFile().isDirectory()
                || !outputPath.toFile().exists()) {
            throw new RuntimeException("config path error");
        }
    }

    public static Path getExcelPath() {
        return excelPath;
    }

    public static Path getExportPath() {
        return exportPath;
    }

    public static Path getOutputPath() {
        return outputPath;
    }

}
