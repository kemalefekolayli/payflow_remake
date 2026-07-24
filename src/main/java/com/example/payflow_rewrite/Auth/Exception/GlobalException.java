    package com.example.payflow_rewrite.Auth.Exception;



    public class GlobalException extends RuntimeException {

        private final ErrorCodes errorCode;

        public GlobalException( ErrorCodes errorCode) {

            this.errorCode = errorCode;
        }

        public ErrorCodes getErrorCode() {
            return errorCode;
        }
    }
