package cn.chauncy.utils.reload.tool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * 热修复类文件提取工具
 *
 * @author chauncy
 */
public class HotfixClassPickOutTool {

    private static final Logger logger = LoggerFactory.getLogger(HotfixClassPickOutTool.class);

    /** 需拷贝class的清单，仅外部类，格式:cn/chauncy/utils/xxx.class */
    private String HOT_FIX_CLASSES_FILE = "./target/hotfixClasses.txt";
    /** 打包的jar文件 */
    private String JAR_FILE = "./target/Common-1.0.jar";
    /** 临时目录 */
    private String TEMP_OUT_PATH = "./temp_out";
    /** 输出目录 */
    private String CLASS_OUT_DIR = "../res/classes";

    public HotfixClassPickOutTool(String configDirName) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configDirName));
        HOT_FIX_CLASSES_FILE = properties.getProperty("HOT_FIX_CLASSES_FILE", HOT_FIX_CLASSES_FILE);
        JAR_FILE = properties.getProperty("JAR_FILE", JAR_FILE);
        TEMP_OUT_PATH = properties.getProperty("TEMP_OUT_PATH", TEMP_OUT_PATH);
        CLASS_OUT_DIR = properties.getProperty("CLASS_OUT_DIR", CLASS_OUT_DIR);
        logger.info("HOT_FIX_CLASSES_FILE: {}", HOT_FIX_CLASSES_FILE);
        logger.info("JAR_FILE: {}", JAR_FILE);
        logger.info("TEMP_OUT_PATH: {}", TEMP_OUT_PATH);
        logger.info("CLASS_OUT_DIR: {}", CLASS_OUT_DIR);
    }

    public static void main(String[] args) {
        try {
            new HotfixClassPickOutTool("").deal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deal() throws Exception {
        // 清理临时目录和目标class输出目录
        cleanDirIfExist(TEMP_OUT_PATH);
        cleanDirIfExist(CLASS_OUT_DIR);

        // 读取并检查class文件信息
        Set<String> classInfoSet = readClassInfoSet();
        preCheckClassInfo(classInfoSet);

        // 拷贝jar中class文件到临时目录
        try (JarFile jarFile = new JarFile(JAR_FILE)) {
            List<JarEntry> classEntrySet = jarFile.stream()
                    .filter(n -> !n.isDirectory() && classInfoSet.contains(getOuterClassPath(n.getName())))
                    .collect(Collectors.toList());

            writeClassFile(jarFile, classEntrySet, TEMP_OUT_PATH, classInfoSet);


            // 校验是否所有的class均拷贝完成
            if (!classInfoSet.isEmpty() || !classEntrySet.isEmpty()) {
                logger.error("class file is invalid: \n{} \n {}",
                        Arrays.toString(classInfoSet.toArray()), Arrays.toString(classEntrySet.toArray()));
                cleanDirIfExist(TEMP_OUT_PATH);
                return;
            }
        }

        // 拷贝到class输出目录并清理临时目录
        File outDir = new File(CLASS_OUT_DIR);
        logger.info("Start copy class file to -> {}", outDir.getAbsolutePath());
        FileUtils.copyDirectory(new File(TEMP_OUT_PATH), outDir);
        cleanDirIfExist(TEMP_OUT_PATH);
        logger.info("Finish.");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeClassFile(JarFile jarFile, List<JarEntry> entries, String path, Set<String> classSet) throws IOException {
        for (Iterator<JarEntry> iterator = entries.iterator(); iterator.hasNext(); ) {
            JarEntry entry = iterator.next();
            InputStream input = jarFile.getInputStream(entry);
            File outFile = new File(path + File.separator + entry.getName());
            outFile.getParentFile().mkdirs();
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            FileOutputStream output = new FileOutputStream(outFile);
            try {
                IOUtils.copy(input, output);
                logger.info("class file path: {}", outFile.getAbsolutePath());
                classSet.remove(getOuterClassPath(entry.getName()));
                iterator.remove();
            } finally {
                IOUtils.closeQuietly(output);
            }
        }
    }

    private String getOuterClassPath(String classpath) {
        String outerClassName;
        if (classpath.contains("$")) {
            outerClassName = classpath.split("\\$")[0];
        } else {
            outerClassName = classpath.replace(".class", "");
        }
        return outerClassName.replace("/", ".");
    }

    private void preCheckClassInfo(Set<String> classInfoSet) {
        for (String classInfo : classInfoSet) {
            if (classInfo.contains("$")) {
                throw new IllegalArgumentException("must be outer class name: " + classInfo);
            }
        }
    }

    private Set<String> readClassInfoSet() throws IOException {
        File file = new File(HOT_FIX_CLASSES_FILE);
        if (!file.isFile() || !file.canRead()) {
            logger.error("file is not exist: {}", file.getAbsolutePath());
            return Set.of();
        }
        List<String> classInfoList = FileUtils.readLines(file, StandardCharsets.UTF_8);
        HashSet<String> classInfoSet = new HashSet<>();
        for (String classInfo : classInfoList) {
            if (!classInfo.isBlank()) {
                classInfoSet.add(classInfo.trim());
            }
        }
        return classInfoSet;
    }

    private void cleanDirIfExist(String dirPath) throws IOException {
        File file = new File(dirPath);
        if (file.exists() && file.isDirectory()) {
            FileUtils.cleanDirectory(file);
        }
    }

}

