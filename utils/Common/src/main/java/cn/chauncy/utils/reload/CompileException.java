package cn.chauncy.utils.reload;

import java.io.Serial;

public class CompileException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CompileException(String message) {
        super(message);
    }

    public CompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
