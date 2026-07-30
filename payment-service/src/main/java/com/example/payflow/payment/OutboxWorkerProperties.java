package com.example.payflow.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbox.worker")
public record OutboxWorkerProperties(
        int batchSize,
        int maxRetries
) {
}
