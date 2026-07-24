package com.example.payflow_rewrite.Auth.Exception;

import org.springframework.http.HttpStatus;

public enum ErrorCodes {

    AUTH_USER_ALREADY_EXISTS(1000, HttpStatus.BAD_REQUEST, "username already exists"),
    AUTH_USER_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "user not found"),
    AUTH_ACCOUNT_NOT_CREATED(1002, HttpStatus.BAD_REQUEST, "account not created"),
    INTERNAL_SERVER_ERROR(9000, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    private final int code;
    private final HttpStatus status;
    private final String message;

    ErrorCodes(int code, HttpStatus Status,String message) {
        this.code = code;
        this.status = Status;
        this.message = message;
    }

    public int getCode() { return code; }
    public HttpStatus getStatus() {return status; }
    public String getMessage() { return message; }

}
