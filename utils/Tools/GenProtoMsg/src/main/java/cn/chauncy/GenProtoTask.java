package cn.chauncy;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生成协议文件
 *
 */
public class GenProtoTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GenProtoTask.class);

    private static String[] INGORE_PROTO_HEADER = new String[]{"syntax", "option"};
    public static void main( String[] args ) {
        new GenProtoTask().run();
    }

    @Override
    public void run() {
        try {
            ProtoExportConfig.loadConfig();

            FileUtils.cleanDirectory(ProtoExportConfig.getJavaOutPath().toFile());
            FileUtils.cleanDirectory(ProtoExportConfig.getTmpPath().toFile());

            copyProtoFile();

            genAllClassFile();
        } catch (Exception e) {
            logger.error("gen proto failed", e);
        }
    }

    private void copyProtoFile() throws Exception {
        File protoFileDir = ProtoExportConfig.getProtoFilePath().toFile();
        File[] protoFiles = protoFileDir.listFiles((file, name) -> name.endsWith(".proto"));
        if (protoFiles == null || protoFiles.length == 0) {
            throw new Exception("proto file not found");
        }

        List<String> headers = new ArrayList<>();
        headers.add("syntax = \"" + ProtoExportConfig.getSYNTAX() + "\";");
        headers.add("option java_multiple_files = " + ProtoExportConfig.isMultipleFiles() + ";");
        headers.add("option java_package = \"" + ProtoExportConfig.getJavaOutPackage() + "\";");

        for (File protoFile : protoFiles) {
            String fileName = protoFile.getName().split("\\.")[0];
            fileName = fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
            List<String> content = Files.readAllLines(protoFile.toPath(), StandardCharsets.UTF_8)
                    .stream()
                    .filter(line -> !line.startsWith("syntax"))
                    .collect(Collectors.toList());
            if (content.isEmpty()) {
                continue;
            }

            Path tempFilePath = ProtoExportConfig.getTmpPath().resolve(protoFile.getName());
            File tempProtoFile = Files.createFile(tempFilePath).toFile();
            FileUtils.writeLines(tempProtoFile, headers);
            FileUtils.writeLines(tempProtoFile, List.of("option java_outer_classname = \"" + fileName + "Msg\";"), true);
            FileUtils.writeLines(tempProtoFile, content, true);
        }
    }

    private void genAllClassFile() throws Exception {
        String cmd = String.format("%s --proto_path %s --java_out=%s %s",
                ProtoExportConfig.getProtocPath(),
                ProtoExportConfig.getTmpPath(),
                ProtoExportConfig.getJavaOutPath(),
                ProtoExportConfig.getTmpPath().toString() + "\\*.proto");

        System.out.println(cmd);
        ProcessUtils.Pair<Integer, String> result = ProcessUtils.exec(cmd);
        logger.info("gen class file, result: {}", result);
    }
}
