package cn.chauncy;


import cn.chauncy.exception.ExcelScanException;
import cn.chauncy.export.ServerExporter;
import cn.chauncy.option.ExcelExportConfig;
import cn.chauncy.option.ExportOption;
import cn.chauncy.option.Mode;
import cn.chauncy.struct.SheetInfo;
import cn.chauncy.util.CommonIOExecutor;
import cn.chauncy.util.ExcelUtil;
import cn.chauncy.validator.ExcelValidator;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.chauncy.util.ExcelUtil.getExcelName;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final FileFilter EXCEL_FILEFILTER = new ExcelFileFilter() ;
    private static final Pattern EXCELNAME_PATTERN = Pattern.compile("[0-9]+_[$a-zA-Z]+_[\\u4e00-\\u9fa5]+");

    public static void main(String[] args) {
        ExcelExportConfig.loadConfig();
        List<Integer> exportIds = getInput();

        ExportOption option = new ExportOption.Builder()
                .excelPath(ExcelExportConfig.getExcelPath())
                .exportIds(exportIds)
                .classOutputPath(ExcelExportConfig.getJavaClassOutPath())
                .jsonOutputPath(ExcelExportConfig.getJavaJsonOutPath())
                .classOutPackage(ExcelExportConfig.getJavaOutPackage())
                .mode(Mode.SERVER)
                .build();

        // 扫描excel文件
        List<SheetInfo> sheetList = scanExcelFile(option.getExcelPath(), option.getExportIds());
        if (sheetList.isEmpty()) {
            logger.error("没有找到需要导出的文件！");
            return;
        }

        // 异步读取excel文件
        long start = System.currentTimeMillis();
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (SheetInfo sheet : Objects.requireNonNull(sheetList)) {
            futures.add(CompletableFuture.supplyAsync(() -> ExcelUtil.readExcel(sheet), CommonIOExecutor.MULTI_EXECUTOR));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long end = System.currentTimeMillis();
        logger.info("解析excel文件完成!, size: [{}], use time: [{}]ms", sheetList.size(), end - start);
        for (CompletableFuture<Boolean> future : futures) {
            if (!future.getNow(false)) {
                throw new RuntimeException("解析excel文件失败！");
            }
        }

        // 验证器验证
        Map<Integer, SheetInfo> sheetMap = sheetList.stream().collect(Collectors.toMap(SheetInfo::getSheetId, v -> v));
        for (ExcelValidator validator : option.getValidators()) {
            if (!validator.validate(sheetMap)) {
                throw new IllegalStateException("验证器验证失败！" + validator.name());
            }
        }

        // 导出json和代码
        try {
            if (option.getMode() == Mode.SERVER) {
                ServerExporter.INSTANCE.export(sheetMap, option);
            }
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }

    }

    private static List<SheetInfo> scanExcelFile(Path excelPath, List<Integer> exportIds) {
        Set<Integer> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        List<SheetInfo> fileList = new ArrayList<>();
        File[] files = excelPath.toFile().listFiles(EXCEL_FILEFILTER);
        for (File f : Objects.requireNonNull(files)) {
            String excelName = getExcelName(f.getName());
            if (!EXCELNAME_PATTERN.matcher(excelName).matches()) {
                throw new ExcelScanException("表名格式(需满足id_name_描述.xlsx)错误：" + f.getName());
            }
            SheetInfo sheetInfo = new SheetInfo(f);
            int id = sheetInfo.getSheetId();
            String name = sheetInfo.getSheetName();
            if (!exportIds.isEmpty() && !exportIds.contains(id)) {
                continue;
            }
            if (id <= 0 || ids.contains(id)) {
                throw new ExcelScanException("发现重复ID的配置表, ID: " + id);
            }
            if (names.contains(name)) {
                throw new ExcelScanException("发现名字重复的配置表, 表名: " + name);
            }
            if (name.startsWith("$")) {
                continue;
            }
            ids.add(id);
            names.add(name);
            fileList.add(sheetInfo);
        }
        return fileList;
    }

    private static List<Integer> getInput() {
        System.out.println("输入要导出的表ID(导全表直接回车，多个表以空格分隔): ");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            List<Integer> list = new ArrayList<>();
            String str = br.readLine().trim();
            if (str.isEmpty() || str.equals("0")) {
                return list;
            }

            String[] ids = str.split(" ");
            for (String id : ids) {
                list.add(Integer.parseInt(id));
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ExcelFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            if (!file.isFile() || !file.canRead()) {
                return false;
            }
            String filename = file.getName();
            if (filename.startsWith("~")) {
                return false;
            }
            return filename.endsWith(".xlsx") || filename.endsWith(".xls") || filename.endsWith(".xlsm");
        }
    }
}
