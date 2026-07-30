package com.example.payflow.notification.service;

import com.example.payflow.notification.NotificationWorkerProperties;
import com.example.payflow.notification.email.EmailSender;
import com.example.payflow.notification.entity.NotificationChannel;
import com.example.payflow.notification.entity.NotificationEntity;
import com.example.payflow.notification.entity.NotificationStatus;
import com.example.payflow.notification.entity.ProcessedEventEntity;
import com.example.payflow.notification.event.TransferCompletedEvent;
import com.example.payflow.notification.repository.NotificationRepository;
import com.example.payflow.notification.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_ERROR_LENGTH = 1000;
    private static final int MAX_BACKOFF_EXPONENT = 12;

    private final NotificationRepository notificationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final EmailSender emailSender;
    private final NotificationWorkerProperties workerProperties;

    @Transactional
    public void handleTransferCompleted(TransferCompletedEvent event) {
        if (processedEventRepository.existsByEventId(event.eventId())) {
            return;
        }

        processedEventRepository.save(
                ProcessedEventEntity.builder()
                        .eventId(event.eventId())
                        .build()
        );

        notificationRepository.save(
                NotificationEntity.builder()
                        .eventId(event.eventId())
                        .userId(event.senderUserId())
                        .channel(NotificationChannel.EMAIL)
                        .recipient(recipientFor(event.senderUserId()))
                        .subject("Transfer completed")
                        .body(buildBody(event))
                        .status(NotificationStatus.PENDING)
                        .retryCount(0)
                        .nextAttemptAt(LocalDateTime.now())
                        .build()
        );
    }

    @Transactional
    public void processPendingNotifications() {
        List<NotificationEntity> notifications =
                notificationRepository.findReadyBatchForUpdate(workerProperties.batchSize());

        for (NotificationEntity notification : notifications) {
            try {
                emailSender.send(
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getBody()
                );
                markSent(notification);
            } catch (Exception exception) {
                markFailedAttempt(notification, exception);
            }
        }
    }

    private String recipientFor(Long userId) {
        return "user-" + userId + "@payflow.local";
    }

    private String buildBody(TransferCompletedEvent event) {
        return "Transfer %s completed. Amount: %s %s."
                .formatted(event.transactionRef(), event.amount(), event.currency());
    }

    private void markSent(NotificationEntity notification) {
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        notification.setLastError(null);
    }

    private void markFailedAttempt(NotificationEntity notification, Exception exception) {
        int retryCount = notification.getRetryCount() + 1;
        notification.setRetryCount(retryCount);
        notification.setLastError(safeErrorMessage(exception));

        if (retryCount >= workerProperties.maxRetries()) {
            notification.setStatus(NotificationStatus.FAILED);
            return;
        }

        notification.setStatus(NotificationStatus.PENDING);
        notification.setNextAttemptAt(
                LocalDateTime.now().plusSeconds(backoffSeconds(retryCount))
        );
    }

    private long backoffSeconds(int retryCount) {
        int exponent = Math.min(Math.max(retryCount - 1, 0), MAX_BACKOFF_EXPONENT);
        return 1L << exponent;
    }

    private String safeErrorMessage(Exception exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            message = cause.getClass().getSimpleName();
        }
        return message.substring(0, Math.min(message.length(), MAX_ERROR_LENGTH));
    }
}
