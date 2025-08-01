package cn.chauncy;

import cn.chauncy.exception.ExcelParseException;
import cn.chauncy.sheet.NormalSheetReader;
import cn.chauncy.sheet.ParamSheetReader;
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

public class ExcelReader2 implements AutoCloseable{

    private  static final Logger logger = LoggerFactory.getLogger(ExcelReader2.class);

    private final Workbook workbook;
    private final SheetInfo sheetInfo;

    public ExcelReader2(SheetInfo sheetInfo) {
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

    public void read() {
        logger.info("开始解析文件: {}", sheetInfo.getFile().getAbsolutePath());
        Sheet sheet = workbook.getSheetAt(sheetInfo.getSheetIndex());
        SheetContent content;
        if (isParamSheet()) {
            content = new ParamSheetReader(sheetInfo.getSheetName()).read(sheet);
        } else {
            content = new NormalSheetReader(sheetInfo.getSheetName()).read(sheet);
        }
        if (content != null && !content.getDataInfoList().isEmpty()) {
            sheetInfo.setSheetContent(content);
        }
        logger.info("解析完成: {}", sheetInfo);
    }

    private boolean isParamSheet() {
        return "global".equalsIgnoreCase(sheetInfo.getSheetName());
    }


    @Override
    public void close() throws Exception {
        IOUtils.close(workbook);
    }
}
