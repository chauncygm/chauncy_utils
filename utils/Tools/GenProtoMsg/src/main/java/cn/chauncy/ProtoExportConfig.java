package cn.chauncy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Pattern;

public class ProtoExportConfig {

    private static final Path CONFIG_PATH = Paths.get("./config/config.properties");

    private static final Pattern pattern = Pattern.compile("^[a-z_]*(\\.[a-z_]*)*$");

    /** 使用proto的语法版本 */
    private static String SYNTAX;
    /** protoc.exe的路径 */
    private static Path PROTOC_PATH;
    /** proto文件存放的路径 */
    private static Path PROTO_FILE_PATH;
    /** 输出java文件的路径 */
    private static Path JAVA_OUT_PATH;
    /** 输出java类文件的所在的包 */
    private static Path JAVA_OUT_PACKAGE;
    /** 生成中间proto文件的路径 */
    private static Path TMP_PATH;

    private static boolean MULTIPLE_FILES;

    public static void loadConfig() throws IOException {
        File configFile = CONFIG_PATH.toFile();
        Properties properties = new Properties();
        properties.load(new FileInputStream(configFile));
        SYNTAX = properties.getProperty("proto.syntax", "proto3");
        MULTIPLE_FILES = Boolean.parseBoolean(properties.getProperty("proto.multiple.files"));

        PROTOC_PATH = Paths.get(properties.getProperty("protoc.path"), "protoc.exe");
        PROTO_FILE_PATH = Paths.get(properties.getProperty("proto.file.path"));
        JAVA_OUT_PATH = Paths.get(properties.getProperty("proto.java.out.path"));
        JAVA_OUT_PACKAGE = Paths.get(properties.getProperty("proto.java.import.package"));
        TMP_PATH = Paths.get(properties.getProperty("proto.tmp.path", "./tmp"));
    }

    public static String getSYNTAX() {
        return SYNTAX;
    }

    public static boolean isMultipleFiles() {
        return MULTIPLE_FILES;
    }

    public static Path getProtocPath() {
        if (PROTOC_PATH == null || !PROTOC_PATH.toFile().isFile()) {
            throw new RuntimeException("protoc.path invalid");
        }
        return PROTOC_PATH;
    }

    public static Path getProtoFilePath() {
        if (PROTO_FILE_PATH == null || !PROTO_FILE_PATH.toFile().isDirectory()) {
            throw new RuntimeException("proto.file.path is not a directory");
        }
        return PROTO_FILE_PATH;
    }

    public static Path getJavaOutPath() {
        if (JAVA_OUT_PATH == null || !JAVA_OUT_PATH.toFile().isDirectory()) {
            throw new RuntimeException("proto.java.out.path is not a directory");
        }
        return JAVA_OUT_PATH;
    }

    public static Path getJavaOutPackage() {
        if (JAVA_OUT_PACKAGE == null || !pattern.matcher(JAVA_OUT_PACKAGE.toString()).matches()) {
            throw new RuntimeException("proto.java.import.package is not a valid package name");
        }
        return JAVA_OUT_PACKAGE;
    }

    public static Path getTmpPath() {
        if (TMP_PATH == null || !TMP_PATH.toFile().isDirectory()) {
            throw new RuntimeException("proto.tmp.path is not a directory");
        }
        return TMP_PATH;
    }
}
