package com.chauncy.utils.reload;

import java.io.Serial;

public class ReloadException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ReloadException(String message) {
        super(message);
    }

    public ReloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
