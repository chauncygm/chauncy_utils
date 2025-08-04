package cn.chauncy.option;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ExcelExportConfig {

    private static final Path CONFIG_PATH = Paths.get("./config/config.properties");

    private static Path excelPath;
    private static Path javaJsonOutPath;

    private static Path javaClassOutPath;
    private static String javaOutPackage;

    public static void loadConfig() {
        File configFile = CONFIG_PATH.toFile();
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(configFile));
        } catch (IOException e) {
            throw new RuntimeException("configFile not exist");
        }
        excelPath = Path.of(properties.getProperty("excel.path"));

        javaJsonOutPath = Path.of(properties.getProperty("java.json.output.path"));
        javaClassOutPath = Paths.get(properties.getProperty("java.class.output.path"));
        javaOutPackage = properties.getProperty("java.class.output.package");

        if (!javaJsonOutPath.toFile().exists()
                || !javaJsonOutPath.toFile().isDirectory()
                || !javaClassOutPath.toFile().exists()) {
            throw new RuntimeException("config path error");
        }
    }

    public static Path getExcelPath() {
        return excelPath;
    }

    public static Path getJavaJsonOutPath() {
        return javaJsonOutPath;
    }

    public static Path getJavaClassOutPath() {
        return javaClassOutPath;
    }

    public static String getJavaOutPackage() {
        return javaOutPackage;
    }

}
