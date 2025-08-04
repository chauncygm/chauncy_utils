package cn.chauncy;

import cn.chauncy.exception.ExcelScanException;
import cn.chauncy.option.ExportOption;
import cn.chauncy.struct.SheetInfo;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static cn.chauncy.util.ExcelUtil.getExcelName;

public class ExcelScanner {

    private static final FileFilter EXCEL_FILEFILTER = new ExcelFileFilter() ;
    private static final Pattern EXCELNAME_PATTERN = Pattern.compile("[0-9]+_[$a-zA-Z]+_[\\u4e00-\\u9fa5]+");

    public static List<SheetInfo> scanExcelFile(ExportOption option) {
        Set<Integer> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        List<SheetInfo> fileList = new ArrayList<>();
        File[] files = option.getExcelPath().toFile().listFiles(EXCEL_FILEFILTER);
        List<Integer> exportIds = option.getExportIds();
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
