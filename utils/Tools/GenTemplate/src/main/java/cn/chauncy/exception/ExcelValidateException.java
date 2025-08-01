package cn.chauncy.exception;

public class ExcelValidateException extends RuntimeException {

    public ExcelValidateException(String message) {
        super(message);
    }

    public ExcelValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelValidateException(Throwable cause) {
        super(cause);
    }
}
