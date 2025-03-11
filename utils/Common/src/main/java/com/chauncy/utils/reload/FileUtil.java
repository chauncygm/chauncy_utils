package com.chauncy.utils.reload;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

public class FileUtil {

    /**
     * 读取文件内容
     *
     * @param fileName 文件名
     * @return 文件内容
     * @throws IOException 文件读取失败
     */
    static byte[] readFile(File file) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)){
            return stream.readAllBytes();
        }
    }

    /**
     * 递归搜索符合要求的文件
     *
     * @param dir       文件目录
     * @param out       输出列表
     * @param predicate 文件过滤器
     */
    static void recurseSearch(@NonNull File dir, @NonNull List<File> out, @Nullable Predicate<? super File> predicate) {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("file error, file not exist or not a dictionary, filePath: " + dir.getPath());
        }

        File[] files = dir.listFiles((file) -> {
            String fileName = file.getName();
            if (fileName.startsWith(".") || fileName.startsWith("~")) {
                return false;
            }
            return file.isDirectory() || predicate == null || predicate.test(file);
        });

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                recurseSearch(file, out, predicate);
            } else {
                out.add(file);
            }
        }
    }

}
