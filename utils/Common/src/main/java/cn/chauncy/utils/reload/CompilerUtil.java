package cn.chauncy.utils.reload;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompilerUtil {

    private static final Logger logger = LoggerFactory.getLogger(CompilerUtil.class);

    private static final JavaCompiler SYSTEM_COMPILER = ToolProvider.getSystemJavaCompiler();

    private CompilerUtil() {}

    public static void compile(@NonNull File javaSourceDir, @NonNull File outputDir) throws IOException {
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

            // 如果有模块路径，使用 --module-path
            String modulePath = buildModulePath();
            if (!modulePath.isEmpty()) {
                options.add("--module-path");
                options.add(modulePath);
            } else {
                // 否则使用传统 classpath
                options.add("-cp");
                options.add(buildClasspath());
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
        String userHome = System.getProperty("user.home");
        if (userHome != null) {
            if (!modulePath.isEmpty()) {
                modulePath.append(File.pathSeparatorChar);
            }
            // 添加.m2/repository 目录（如果存在）
            modulePath.append(userHome).append(File.separator)
                    .append(".m2").append(File.separator).append("repository");
        }

        String result = modulePath.toString();
        logger.debug("Built module path: {}", result);
        return result;
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
