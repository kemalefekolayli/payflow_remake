package com.example.paytrans.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.worker")
public record NotificationWorkerProperties(
        int batchSize,
        int maxRetries
) {
}
