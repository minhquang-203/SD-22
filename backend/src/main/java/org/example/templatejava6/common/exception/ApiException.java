package org.example.templatejava6.common.exception;

public class ApiException extends RuntimeException {

    private final String code;
    private final Object details;

    public ApiException(String message, String code) {
        this(message, code, null);
    }

    public ApiException(String message, String code, Object details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public Object getDetails() {
        return details;
    }
}
