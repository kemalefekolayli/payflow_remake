package com.example.paytrans.payment.service;

import com.example.paytrans.payment.OutboxWorkerProperties;
import com.example.paytrans.payment.entity.OutboxEventEntity;
import com.example.paytrans.payment.enums.OutboxStatus;
import com.example.paytrans.payment.event.TransferCompletedEvent;
import com.example.paytrans.payment.repository.OutboxEventRepository;
import com.example.paytrans.payment.worker.KafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private static final String TRANSFER_COMPLETED = "TRANSFER_COMPLETED";
    private static final String TRANSACTION = "TRANSACTION";
    private static final int MAX_ERROR_LENGTH = 1000;
    private static final int MAX_BACKOFF_EXPONENT = 12;

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final OutboxWorkerProperties workerProperties;

    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEventEntity createTransferCompletedEvent(TransferCompletedEvent event) {
        String payload = objectMapper.writeValueAsString(event);

        OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                .eventId(event.eventId())
                .eventType(TRANSFER_COMPLETED)
                .aggregateType(TRANSACTION)
                .aggregateId(event.transactionId())
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .nextAttemptAt(LocalDateTime.now())
                .build();

        return outboxEventRepository.save(outboxEvent);
    }

    @Transactional
    public void processPendingEvents() {
        List<OutboxEventEntity> events =
                outboxEventRepository.findReadyEvents(workerProperties.batchSize());

        for (OutboxEventEntity event : events) {
            try {
                kafkaEventPublisher.publish(event);
                markPublished(event);
            } catch (Exception exception) {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                markFailedAttempt(event, exception);
            }
        }
    }

    private void markPublished(OutboxEventEntity event) {
        event.setStatus(OutboxStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        event.setLastError(null);
    }

    private void markFailedAttempt(OutboxEventEntity event, Exception exception) {
        int retryCount = event.getRetryCount() + 1;
        event.setRetryCount(retryCount);
        event.setLastError(safeErrorMessage(exception));

        if (retryCount >= workerProperties.maxRetries()) {
            event.setStatus(OutboxStatus.FAILED);
            return;
        }

        event.setStatus(OutboxStatus.PENDING);
        event.setNextAttemptAt(LocalDateTime.now().plusSeconds(backoffSeconds(retryCount)));
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
