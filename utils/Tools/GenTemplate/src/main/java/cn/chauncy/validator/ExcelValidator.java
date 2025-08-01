package cn.chauncy.validator;

import cn.chauncy.exception.ExcelValidateException;
import cn.chauncy.struct.SheetInfo;

import java.util.Map;

public interface ExcelValidator {

    ExcelValidator DEFAULT = new ExcelValidator() {
        @Override
        public String name() {
            return "";
        }

        @Override
        public boolean validate(Map<Integer, SheetInfo> excelFilePath) {
            return true;
        }
    };

    String name();

    /** 是否验证通过 */
    boolean validate(Map<Integer, SheetInfo> excelFilePath) throws ExcelValidateException;

}
