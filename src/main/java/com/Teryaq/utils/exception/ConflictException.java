package com.Teryaq.utils.exception;

public class ConflictException extends RuntimeException{
    public ConflictException(String message) {
        super(message);
    }
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
