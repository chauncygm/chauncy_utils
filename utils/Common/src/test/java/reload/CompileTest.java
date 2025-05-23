package reload;

import cn.chauncy.utils.reload.CompilerUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

public class CompileTest {

    @Test
    public void testCompile() {
        System.out.println(new File(".").getAbsolutePath());

        File sourceDir = Path.of("src/main/java").toFile();
        File outputDir = Path.of("res/classes").toFile();
        try {
            CompilerUtil.compile(sourceDir, outputDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
