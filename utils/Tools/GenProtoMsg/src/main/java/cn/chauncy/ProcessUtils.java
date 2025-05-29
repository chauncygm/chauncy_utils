package cn.chauncy;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ProcessUtils {

    private static final Pair<Integer, String> SUCCESS = new Pair<>(0, "SUCCESS");

    public static Pair<Integer, String> exec(String cmd) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(cmd);

        int exitValue = process.waitFor();
        if (exitValue == 0) {
            return SUCCESS;
        }

        try(BufferedReader bufferedReader = process.errorReader(StandardCharsets.UTF_8)) {
            String collect = bufferedReader
                    .lines()
                    .collect(Collectors.joining("\n"));
            return Pair.of(exitValue, collect);
        }
    }

    public record Pair<F, S>(F first, S second) {
        public static <F, S> Pair<F, S> of(F first, S second) {
            return new Pair<>(first, second);
        }

    }
}
