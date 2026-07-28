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
    WALLET_ALREADY_FROZEN(2002, HttpStatus.BAD_REQUEST, "Wallet is already frozen"),
    WALLET_SENDER_NOT_FOUND(2003, HttpStatus.NOT_FOUND, "Wallet not found - sender"),
    WALLET_RECEIVER_NOT_FOUND(2004, HttpStatus.NOT_FOUND, "Wallet not found - receiver"),
    WALLET_NOT_ACTIVE_SENDER(2005, HttpStatus.NOT_FOUND, "Wallet not active"),
    WALLET_NOT_ACTIVE_RECEIVER(2006, HttpStatus.NOT_FOUND, "Wallet not active"),
    WALLET_SAME(2007, HttpStatus.BAD_REQUEST, "Sender and receiver cant be the same wallet"),
    WALLET_BALANCE_NOT_ENOUGH(2008, HttpStatus.BAD_REQUEST,"Balance not enough"),
    WALLET_NOT_FOUND(2000, HttpStatus.NOT_FOUND, "Wallet not found"),

    TRANSACTION_NOT_FOUND(3001, HttpStatus.NOT_FOUND,"Transaction could not be found"),
    TRANSACTION_CURRENCY_MISMATCH(3000, HttpStatus.BAD_REQUEST, "Currency does not match");


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
