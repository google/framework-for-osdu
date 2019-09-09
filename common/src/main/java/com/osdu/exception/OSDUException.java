package com.osdu.exception;

public class OSDUException extends RuntimeException {

    public OSDUException(String message) {
        super(message);
    }

    public OSDUException(String message, Throwable cause) {
        super(message, cause);
    }
}