package com.example.paytrans.payment.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(GlobalException exception) {
        ErrorCodes code = exception.getErrorCode();
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                code.getStatus().value(),
                code.getCode(),
                exception.getMessage(),
                code.getStatus().getReasonPhrase()
        );
        return new ResponseEntity<>(body, code.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
        log.error("Unexpected error", exception);
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
