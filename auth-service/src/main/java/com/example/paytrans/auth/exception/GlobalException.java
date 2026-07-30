package com.example.paytrans.auth.exception;

public class GlobalException extends RuntimeException {

    private final ErrorCodes errorCode;

    public GlobalException(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public GlobalException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCodes getErrorCode() {
        return errorCode;
    }
}
