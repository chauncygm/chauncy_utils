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

        String classpath = System.getProperty("java.class.path");
        logger.debug("Classpath: {}", classpath);

        final DiagnosticCollector<? super JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = SYSTEM_COMPILER.getStandardFileManager(diagnostics, null, null)) {
            // 1.搜索java文件
            List<File> javaFiles = new ArrayList<>();
            FileUtil.recurseSearch(javaSourceDir, javaFiles, (file) -> file.getName().endsWith(".java"));
            // 2.转换为JavaFileObject集合
            Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjectsFromFiles(javaFiles);
            // 3.配置编译参数
            List<String> options = Arrays.asList("-encoding", "UTF-8", "-cp", classpath, "-d", outputDir.getPath());
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
        }
    }
}
