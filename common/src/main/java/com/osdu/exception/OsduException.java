package com.osdu.exception;

public class OsduException extends RuntimeException {

    public OsduException(String message) {
        super(message);
    }

    public OsduException(String message, Throwable cause) {
        super(message, cause);
    }
}