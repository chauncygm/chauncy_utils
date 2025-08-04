package cn.chauncy.reader;

import cn.chauncy.exception.ExcelParseException;
import cn.chauncy.option.ExportOption;
import cn.chauncy.struct.SheetContent;
import cn.chauncy.struct.SheetInfo;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ExcelReader implements AutoCloseable{

    private  static final Logger logger = LoggerFactory.getLogger(ExcelReader.class);

    private final Workbook workbook;
    private final SheetInfo sheetInfo;

    public ExcelReader(SheetInfo sheetInfo) {
        this.sheetInfo = sheetInfo;
        File file = sheetInfo.getFile();
        try {
            workbook = new XSSFWorkbook(new FileInputStream(file));
            if (workbook.getNumberOfSheets() < 1) {
                throw new ExcelParseException("工作表不存在");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void read(ExportOption option) {
        logger.info("开始解析文件: {}", sheetInfo.getFile().getAbsolutePath());
        Sheet sheet = workbook.getSheetAt(sheetInfo.getSheetIndex());
        SheetContent content;
        if (isParamSheet()) {
            content = new ParamSheetReader(sheetInfo.getSheetName()).read(sheet, option);
        } else {
            content = new NormalSheetReader(sheetInfo.getSheetName()).read(sheet, option);
        }
        if (content != null && !content.getDataInfoList().isEmpty()) {
            sheetInfo.setSheetContent(content);
        }
        logger.info("解析文件完成: {}", sheetInfo);
    }

    private boolean isParamSheet() {
        return "global".equalsIgnoreCase(sheetInfo.getSheetName());
    }


    @Override
    public void close() throws Exception {
        IOUtils.close(workbook);
    }
}
