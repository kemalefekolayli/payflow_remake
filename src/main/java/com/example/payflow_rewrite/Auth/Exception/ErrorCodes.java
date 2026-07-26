package com.example.payflow_rewrite.Auth.Exception;

import org.springframework.http.HttpStatus;

public enum ErrorCodes {

    // auth
    AUTH_USER_ALREADY_EXISTS(1000, HttpStatus.BAD_REQUEST, "username already exists"),
    AUTH_USER_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "user not found"),
    AUTH_ACCOUNT_NOT_CREATED(1002, HttpStatus.BAD_REQUEST, "account not created"),
    INTERNAL_SERVER_ERROR(9000, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error"),

    //wallet
    WALLET_ALREADY_EXIST(2001,HttpStatus.BAD_REQUEST, "Wallet already exists"),
    WALLET_NOT_FOUND(2002, HttpStatus.NOT_FOUND, "Wallet not found");


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
