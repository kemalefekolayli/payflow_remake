package com.example.paytrans.notification.worker;

import com.example.paytrans.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "notification.worker.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationDeliveryWorker {

    private final NotificationService notificationService;

    @Scheduled(fixedDelayString = "${notification.worker.delay-ms:1000}")
    public void deliverReadyNotifications() {
        notificationService.processPendingNotifications();
    }
}
