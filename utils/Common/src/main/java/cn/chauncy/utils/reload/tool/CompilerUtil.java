package cn.chauncy.utils.reload.tool;

import cn.chauncy.utils.reload.CompileException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.tools.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompilerUtil {

    private static final Logger logger = LoggerFactory.getLogger(CompilerUtil.class);

    private static final JavaCompiler SYSTEM_COMPILER = ToolProvider.getSystemJavaCompiler();

    private CompilerUtil() {}

    /**
     * 编译指定目录下的java文件
     *
     * @param javaSourceDir 要求编译的路径是该模块源根目录，编译整个模块，即全量编译。
     * @param outputDir 输出目录
     */
    public static void compile(@NonNull File javaSourceDir, @NonNull File outputDir) {
        if (SYSTEM_COMPILER == null) {
            throw new IllegalStateException("JDK compiler not available (are you using JRE?)");
        }
        if (!javaSourceDir.exists()) {
            throw new IllegalArgumentException("Java source directory does not exist: " + javaSourceDir);
        }
        // 输出目录
        if (!outputDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            outputDir.mkdirs();
        }


        final DiagnosticCollector<? super JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = SYSTEM_COMPILER.getStandardFileManager(diagnostics, null, null)) {
            // 1.搜索java文件
            List<File> javaFiles = new ArrayList<>();
            FileUtil.recurseSearch(javaSourceDir, javaFiles, (file) -> file.getName().endsWith(".java"));
            if (javaFiles.isEmpty()) {
                logger.warn("No Java source files found in {}", javaSourceDir);
                return;
            }

            // 2.转换为JavaFileObject集合
            Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjectsFromFiles(javaFiles);

            // 3.配置编译参数 - 使用模块路径
            String outputDirPath = outputDir.getPath();
            List<String> options = new ArrayList<>(List.of("-encoding", "UTF-8", "-d", outputDirPath, "-source", "17", "-target", "17"));

            String modulePath = buildModulePath();
            if (!modulePath.isEmpty()) {
                options.add("--module-path");
                options.add(modulePath);
            }

            String classpath = buildClasspath();
            if (!classpath.isEmpty()) {
                options.add("-cp");
                options.add(classpath);
            }
            logger.info("Compile options: {}", options);

            // 4.创建编译任务
            JavaCompiler.CompilationTask task = SYSTEM_COMPILER.getTask(null, fileManager, null, options, null, javaFileObjects);
            // 5. 执行编译并处理结果
            if (!task.call()) {
                diagnostics.getDiagnostics().forEach(d ->
                        logger.error("Compilation error: [{}] {}", d.getLineNumber(), d.getMessage(null))
                );
                throw new CompileException("Failed to compile sources in " + javaSourceDir);
            }
            logger.info("Successfully compiled {} Java files", javaFiles.size());
        } catch (Throwable e) {
            logger.error(e.toString(), e);
        }
    }

    /**
     * 构建模块路径
     */
    private static String buildModulePath() {
        StringBuilder modulePath = new StringBuilder();

        // 1. 尝试从系统属性获取
        String sysModulePath = System.getProperty("jdk.module.path");
        if (sysModulePath != null && !sysModulePath.isEmpty()) {
            modulePath.append(sysModulePath);
        }

        // 2. 添加 Maven 仓库中的模块
        String mvnRepoPath = getMavenLocalRepositoryPath();
        if (!mvnRepoPath.isEmpty()) {
            modulePath.append(File.pathSeparatorChar).append(mvnRepoPath);
        }

        String result = modulePath.toString();
        logger.debug("Built module path: {}", result);
        return result;
    }
    /**
     * 获取 Maven 本地仓库路径
     * 优先级：系统属性 > settings.xml 配置 > 默认路径
     */
    private static String getMavenLocalRepositoryPath() {
        // 优先级 1: 系统属性指定（mvn -Dmaven.repo.local=xxx）
        String repoPath = System.getProperty("maven.repo.local");
        if (repoPath != null && !repoPath.isEmpty() && new File(repoPath).exists()) {
            logger.debug("Found maven repo from system property: {}", repoPath);
            return repoPath;
        }


        // 优先级 2: 环境变量 MAVEN_HOME + conf/settings.xml
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome == null) {
            mavenHome = System.getenv("M2_HOME");
        }
        if (mavenHome != null) {
            File settingsFile = new File(mavenHome, "conf" + File.separator + "settings.xml");
            repoPath = parseSettingsXml(settingsFile);
            if (!repoPath.isEmpty()) {
                logger.debug("Found maven repo from MAVEN_HOME settings: {}", repoPath);
                return repoPath;
            }
        }

        // 优先级 3: 用户目录下的 .m2/settings.xml
        String userHome = System.getProperty("user.home");
        if (userHome != null) {
            File userSettings = new File(userHome, ".m2" + File.separator + "settings.xml");
            repoPath = parseSettingsXml(userSettings);
            if (!repoPath.isEmpty()) {
                logger.debug("Found maven repo from user settings: {}", repoPath);
                return repoPath;
            }
        }

        // 优先级 4: 默认路径 ~/.m2/repository
        if (userHome != null) {
            File defaultRepo = new File(userHome, ".m2" + File.separator + "repository");
            if (defaultRepo.exists()) {
                logger.debug("Using default maven repo: {}", defaultRepo.getAbsolutePath());
                return defaultRepo.getAbsolutePath();
            }
        }
        logger.warn("Maven local repository not found, using empty path");
        return "";
    }

    /**
     * 解析 settings.xml 获取本地仓库路径
     */
    private static String parseSettingsXml(File settingsFile) {
        if (!settingsFile.exists()) {
            return "";
        }

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(settingsFile);

            NodeList localRepoNodes = doc.getElementsByTagName("localRepository");
            if (localRepoNodes.getLength() > 0) {
                String localRepo = localRepoNodes.item(0).getTextContent().trim();
                if (!localRepo.isEmpty()) {
                    // 处理变量替换（如 ${user.home}）
                    localRepo = resolveVariables(localRepo);
                    File repoDir = new File(localRepo);
                    if (repoDir.exists()) {
                        return repoDir.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse settings.xml: {}", e.getMessage());
        }

        return "";
    }

    /**
     * 解析 XML 中的变量（如 ${user.home}）
     */
    private static String resolveVariables(String text) {
        if (text.contains("${user.home}")) {
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                text = text.replace("${user.home}", userHome);
            }
        }
        if (text.contains("${basedir}")) {
            String userDir = System.getProperty("user.dir");
            if (userDir != null) {
                text = text.replace("${basedir}", userDir);
            }
        }
        // 可以扩展支持更多系统属性
        return text;
    }

    /**
     * 构建类路径（非模块化环境使用）
     */
    private static String buildClasspath() {
        StringBuilder classpath = new StringBuilder();

        // 1. 添加当前 classpath
        String currentClasspath = System.getProperty("java.class.path", "");
        if (!currentClasspath.isEmpty()) {
            classpath.append(currentClasspath);
        }

        // 2. 添加当前工作目录的 target/classes
        String userDir = System.getProperty("user.dir");
        if (userDir != null) {
            if (!classpath.isEmpty()) {
                classpath.append(File.pathSeparatorChar);
            }
            classpath.append(userDir).append(File.separator).append("target").append(File.separator).append("classes");
        }

        String result = classpath.toString();
        logger.debug("Built classpath: {}", result);
        return result;
    }
}
