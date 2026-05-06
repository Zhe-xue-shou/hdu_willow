package com.hdu.hdufpga.exception;

public class UserQueueException extends Exception {
    private final ExceptionType type;

    public enum ExceptionType {
        RECOVERABLE,     // 可修复
        IRRECOVERABLE    // 不可修复
    }

    public UserQueueException(String s, ExceptionType type) {
        super(s);
        this.type = type;
    }

    public boolean isRecoverable() {
        return type == ExceptionType.RECOVERABLE;
    }

    public ExceptionType getType() {
        return type;
    }
}