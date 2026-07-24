package com.example.payflow_rewrite.Auth.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> HandleCustomException(GlobalException ex){

        ErrorCodes code = ex.getErrorCode();
        ErrorResponse body = new ErrorResponse(Instant.now(), code.getStatus().value(),code.getCode(),code.getMessage(),code.getStatus().getReasonPhrase());

        return new ResponseEntity<ErrorResponse>(body, code.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        log.error("Unexpected error", ex);

        ErrorCodes code = ErrorCodes.INTERNAL_SERVER_ERROR;

        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                code.getStatus().value(),
                code.getCode(),
                code.getMessage(),
                code.getStatus().getReasonPhrase()
        );

        return new ResponseEntity<>(body, code.getStatus());
    }
}
