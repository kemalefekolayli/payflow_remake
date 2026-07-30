package com.example.paytrans.auth.exception;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, int code, String message, String error) {
}
