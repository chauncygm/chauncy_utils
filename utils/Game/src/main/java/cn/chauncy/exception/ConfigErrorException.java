package cn.chauncy.exception;

import java.io.Serial;

public class ConfigErrorException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConfigErrorException(String name, int configId, String message) {
        super(name + "[" + configId + "]" + "config error, reason: " + message);
    }

}
