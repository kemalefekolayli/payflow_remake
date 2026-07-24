package com.example.payflow_rewrite.Auth.Exception;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, int code, String message, String error) {
}
