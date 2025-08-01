package cn.chauncy.exception;

public class ExcelScanException extends RuntimeException {

    public ExcelScanException(String message) {
        super(message);
    }

    public ExcelScanException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelScanException(Throwable cause) {
        super(cause);
    }
}
