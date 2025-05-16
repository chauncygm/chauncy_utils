package cn.chauncy;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 生成协议文件
 *
 */
public class GenProtoTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GenProtoTask.class);
    public static void main( String[] args ) {
        new GenProtoTask().run();
    }

    @Override
    public void run() {
        try {
            ProtoConfig.loadConfig();

            FileUtils.cleanDirectory(ProtoConfig.getJavaOutPath().toFile());
            FileUtils.cleanDirectory(ProtoConfig.getTmpPath().toFile());

            copyProtoFile();

            genAllClassFile();
        } catch (Exception e) {
            logger.error("gen proto failed", e);
        }
    }

    private void copyProtoFile() throws Exception {
        File protoFileDir = ProtoConfig.getProtoFilePath().toFile();
        File[] protoFiles = protoFileDir.listFiles((file, name) -> name.endsWith(".proto"));
        if (protoFiles == null || protoFiles.length == 0) {
            throw new Exception("proto file not found");
        }

        for (File protoFile : protoFiles) {
            List<String> content = Files.readAllLines(protoFile.toPath(), StandardCharsets.UTF_8);
            if (content.isEmpty()) {
                continue;
            }

            Path tempFilePath = ProtoConfig.getTmpPath().resolve(protoFile.getName());
            File tempProtoFile = Files.createFile(tempFilePath).toFile();
            FileUtils.writeLines(tempProtoFile, content);
        }
    }

    private void genAllClassFile() throws Exception {
        String cmd = String.format("%s --proto_path %s --java_out=%s %s",
                ProtoConfig.getProtocPath(),
                ProtoConfig.getTmpPath(),
                ProtoConfig.getJavaOutPath(),
                ProtoConfig.getTmpPath().toString() + "\\*.proto");

        System.out.println(cmd);
        Pair<Integer, String> result = ProcessUtils.exec(cmd);
        logger.info("gen class file, result: {}", result);
    }
}
