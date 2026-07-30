package com.example.paytrans.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbox.worker")
public record OutboxWorkerProperties(
        int batchSize,
        int maxRetries
) {
}
