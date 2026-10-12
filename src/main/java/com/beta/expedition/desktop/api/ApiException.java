package com.beta.expedition.desktop.api;

public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(String message) {
        this(0, message, null);
    }

    public ApiException(int status, String message) {
        this(status, message, null);
    }

    public ApiException(String message, Throwable cause) {
        this(0, message, cause);
    }

    public ApiException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
