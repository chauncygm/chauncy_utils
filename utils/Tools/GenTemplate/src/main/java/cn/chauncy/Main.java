package cn.chauncy;

import cn.chauncy.export.ServerExporter;
import cn.chauncy.option.ExcelExportConfig;
import cn.chauncy.option.ExportOption;
import cn.chauncy.option.Mode;
import cn.chauncy.struct.SheetInfo;
import cn.chauncy.util.CommonIOExecutor;
import cn.chauncy.util.ExcelUtil;
import cn.chauncy.validator.ExcelValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ExcelExportConfig.loadConfig();
        Mode mode = args.length > 0 && "client".equalsIgnoreCase(args[0]) ? Mode.CLIENT : Mode.SERVER;
        List<Integer> exportIds = Arrays.stream(args)
                .skip(1)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        ExportOption option = new ExportOption.Builder()
                .excelPath(ExcelExportConfig.getExcelPath())
                .classOutputPath(ExcelExportConfig.getJavaClassOutPath())
                .jsonOutputPath(ExcelExportConfig.getJavaJsonOutPath())
                .classOutPackage(ExcelExportConfig.getJavaOutPackage())
                .mode(mode)
                .exportIds(exportIds)
                .build();

        // 扫描excel文件
        List<SheetInfo> sheetList = ExcelScanner.scanExcelFile(option);
        if (sheetList.isEmpty()) {
            logger.error("没有找到需要导出的文件！");
            return;
        }

        // 异步读取excel文件
        long start = System.currentTimeMillis();
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (SheetInfo sheet : Objects.requireNonNull(sheetList)) {
            futures.add(CompletableFuture.supplyAsync(() -> ExcelUtil.readExcel(sheet, option), CommonIOExecutor.MULTI_EXECUTOR));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        logger.info("解析excel文件完成!, size: [{}], use time: [{}]ms", sheetList.size(), System.currentTimeMillis() - start);
        for (CompletableFuture<Boolean> future : futures) {
            if (!future.getNow(false)) {
                throw new RuntimeException("解析excel文件失败！");
            }
        }

        // 验证器验证
        Map<Integer, SheetInfo> sheetMap = sheetList.stream()
                .filter(v -> v.getSheetContent() != null && !v.getSheetContent().getFieldInfoMap().isEmpty())
                .collect(Collectors.toMap(SheetInfo::getSheetId, v -> v));
        for (ExcelValidator validator : option.getValidators()) {
            if (!validator.validate(sheetMap)) {
                throw new IllegalStateException("验证器验证失败！" + validator.name());
            }
        }

        // 导出json和代码
        if (option.getMode() == Mode.SERVER) {
            ServerExporter.INSTANCE.export(sheetMap, option);
        }

    }
}
