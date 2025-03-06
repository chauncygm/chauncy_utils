package com.chauncy.utils.reload;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

public class ReloadUtil {

    static byte[] readFile(String fileName) throws IOException {
        try (FileInputStream stream = new FileInputStream(fileName)){
            return stream.readAllBytes();
        }
    }

    static void recurseSearch(@NonNull File dir, @NonNull List<File> out, @Nullable Predicate<? super File> predicate) {
        if (!dir.exists()) {
            return;
        }

        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("file type error, file not a dictionary, filePath: " + dir.getPath());
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
