package cn.chauncy.utils.reload;

import cn.chauncy.utils.common.ExceptionUtils;
import cn.chauncy.utils.time.StopWatch;
import com.google.common.base.Objects;
import org.apache.commons.codec.digest.DigestUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassReloader {

    private static final Logger logger = LoggerFactory.getLogger(ClassReloader.class);
    private static final String CLASS_SUFFIX = ".class";

//    /** 资源路径 */
//    private final Path resourcePath;
//    /** class文件相对路径 */
//    private final Path classDirRelativePath;
    /** class文件绝对路径 */
    private final Path classDirPath;

    /** class摘要信息 */
    private final Map<String, ClassStat> fileStatMap = new HashMap<>();

    public ClassReloader(@NonNull String resPath, @NonNull String classDirPath) {
//        this.resourcePath = Path.of(resPath);
//        this.classDirRelativePath = Path.of(classDirPath);
        this.classDirPath = Path.of(resPath, classDirPath).normalize();
        if (!Files.isDirectory(this.classDirPath)) {
            throw new IllegalArgumentException("classAbsolutePath not exists");
        }
    }

    /**
     * 热更指定资源目录下的class
     *
     */
    public void reloadAllClass() {
        logger.info("start reloadAllClass");
        StopWatch stopWatch = StopWatch.create("ReloadAllClassTask");

        try {
            // 1.加载所有的class定义
            List<ClassDefinition> classes = findAllClassDefinition();
            stopWatch.logStep("findAllClass");

            for (ClassDefinition aClass : classes) {
                logger.info("reloadAllClass, load class: {}", aClass.getDefinitionClass());
            }

            // 2.热更所有找到的class定义
            refineAllClass(classes);
            stopWatch.logStep("refineAllClass");

            for (ClassDefinition aClass : classes) {
                logger.info("reloadAllClass, redefine class: {}", aClass.getDefinitionClass());
            }

            // 3.统计打印热更后的class数量
            long outerClassNum = getOuterClassNum(classes);
            logger.info("reloadAllClass completed, outer class loaded: {}",outerClassNum);
        } catch (Exception e) {
            logger.error("reloadAll failure, stepInfo: {}", stopWatch);
            ExceptionUtils.rethrow(new ReloadException("Reload classes failure", e));
        }
    }

    /**
     * 获取外部类的数量
     *
     * @param classes class定义列表
     * @return 外部类数量
     */
    private static long getOuterClassNum(List<ClassDefinition> classes) {
        return classes.stream()
                .filter(c -> !c.getDefinitionClass().getName().contains("$"))
                .count();
    }

    /**
     * 热更所有找到的class定义
     *
     * @param classes class定义列表
     * @throws UnmodifiableClassException 无法修改指定的类
     * @throws ClassNotFoundException 不会抛出，为兼容性存在
     */
    private void refineAllClass(List<ClassDefinition> classes) throws UnmodifiableClassException, ClassNotFoundException {
        if (!classes.isEmpty()) {
            // 执行热更新
            InstHelper.redefineClasses(classes.toArray(new ClassDefinition[0]));

            Map<String, ClassStat> updatedFileStatMap = getUpdatedFileStatMap(classes);
            fileStatMap.putAll(updatedFileStatMap);
        }
    }

    /**
     * 获取更新后的文件状态
     *
     * @param classes class定义列表
     * @return 文件状态map
     */
    private Map<String, ClassStat> getUpdatedFileStatMap(List<ClassDefinition> classes) {
        Map<String, ClassStat> updatedFileStatMap = new HashMap<>(classes.size());
        for (ClassDefinition aClass : classes) {
            String className = aClass.getDefinitionClass().getName();
            byte[] bytes = aClass.getDefinitionClassFile();
            String md5Hex = DigestUtils.md5Hex(bytes);
            ClassStat fileStat = new ClassStat(md5Hex, bytes.length);
            updatedFileStatMap.put(className, fileStat);
        }
        return updatedFileStatMap;
    }

    /**
     * 获取所有class定义
     *
     * @throws IOException 读取文件失败
     * @throws ClassNotFoundException 加载class失败，找不到对应的class
     *
     * @return 所有的class定义
     */
    private List<ClassDefinition> findAllClassDefinition() throws IOException, ClassNotFoundException {
        // 找到所有的class文件
        List<File> classFiles = findAllClassFile();

        List<ClassDefinition> classes = new ArrayList<>(classFiles.size());
        for (File classFile : classFiles) {
            String className = findClassName(classFile);

            Class<?> clazz = Class.forName(className);
            byte[] bytes = FileUtil.readFile(classFile);

            // 排除已经加载过的class文件
            ClassStat oldFileStat = fileStatMap.get(className);
            ClassStat fileStat = checkModifiedFileStat(bytes, oldFileStat);
            if (fileStat == oldFileStat) {
                logger.debug("Class [{}] not modified, skip reloading", className);
                continue;
            }

            ClassDefinition definition = new ClassDefinition(clazz, bytes);
            classes.add(definition);
        }
        return classes;
    }

    /**
     * 获取文件摘要状态，对比md5摘要
     *
     * @param bytes     文件bytes
     * @param fileStat  旧的文件状态
     * @return  若byte无变化，则返回旧文件状态，否则返回新的文件状态
     */
    private ClassStat checkModifiedFileStat(byte[] bytes, @Nullable ClassStat fileStat) {
        String md5Hex = DigestUtils.md5Hex(bytes);
        if (fileStat == null) {
            return new ClassStat(md5Hex, bytes.length);
        }
        if (!Objects.equal(fileStat.md5, md5Hex) || fileStat.length != bytes.length) {
            return new ClassStat(md5Hex, bytes.length);
        }
        return fileStat;
    }

    /**
     * 获取class全限定类名
     *
     * @param classFile class文件
     * @return class全限定类名
     */
    private String findClassName(File classFile) {
        Path relativePath = classDirPath.relativize(classFile.toPath());
        String className = relativePath.toString().replace(File.separator, ".");
        className = className.replace(CLASS_SUFFIX, "");
        return className;
    }

    /**
     * 获取所有class文件
     * 从资源目录指定路径递归查找class文件
     *
     * @return class文件列表
     */
    private List<File> findAllClassFile() {
        List<File> files = new ArrayList<>();
        File classDir = classDirPath.toFile();
        FileUtil.recurseSearch(classDir, files, (file) -> file.getName().endsWith(CLASS_SUFFIX));
        return files;
    }

    /**
     * class文件摘要状态
     */
    record ClassStat(String md5, int length) {

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof ClassStat other) {
                return this.md5.equalsIgnoreCase(other.md5) && this.length == other.length;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(md5, length);
        }
    }
}
