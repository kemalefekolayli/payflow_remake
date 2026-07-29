package com.example.payflow.payment.error;

import org.springframework.http.HttpStatus;

public enum ErrorCodes {
    INTERNAL_SERVER_ERROR(9000, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error"),
    WALLET_NOT_FOUND(2000, HttpStatus.NOT_FOUND, "Wallet not found"),
    WALLET_ALREADY_EXIST(2001, HttpStatus.BAD_REQUEST, "Wallet already exists"),
    WALLET_ALREADY_FROZEN(2002, HttpStatus.BAD_REQUEST, "Wallet is already frozen"),
    WALLET_NOT_ACTIVE_SENDER(2005, HttpStatus.NOT_FOUND, "Wallet not active"),
    WALLET_NOT_ACTIVE_RECEIVER(2006, HttpStatus.NOT_FOUND, "Wallet not active"),
    WALLET_SAME(2007, HttpStatus.BAD_REQUEST, "Sender and receiver cant be the same wallet"),
    WALLET_BALANCE_NOT_ENOUGH(2008, HttpStatus.BAD_REQUEST, "Balance not enough"),
    TRANSACTION_CURRENCY_MISMATCH(3000, HttpStatus.BAD_REQUEST, "Currency does not match"),
    TRANSACTION_NOT_FOUND(3001, HttpStatus.NOT_FOUND, "Transaction could not be found"),
    LEDGER_NOT_BALANCED(4000, HttpStatus.BAD_REQUEST, "Ledger error occured"),
    LEDGER_NOT_FOUND(4001, HttpStatus.NOT_FOUND, "Ledger entries could not be found");

    private final int code;
    private final HttpStatus status;
    private final String message;

    ErrorCodes(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
