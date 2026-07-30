package com.example.paytrans.payment;

import com.example.paytrans.payment.entity.OutboxEventEntity;
import com.example.paytrans.payment.enums.OutboxStatus;
import com.example.paytrans.payment.repository.OutboxEventRepository;
import com.example.paytrans.payment.service.OutboxService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = PaymentServiceApplication.class,
        properties = "spring.datasource.url=jdbc:h2:mem:outbox_publisher_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
)
@Transactional
class OutboxPublisherIntegrationTest {

    private static final String TOPIC = "payment.transfer-completed";

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void readyPendingEventIsPublishedAndMarkedPublished() {
        OutboxEventEntity event = saveEvent(OutboxStatus.PENDING, 0, LocalDateTime.now().minusSeconds(1));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        outboxService.processPendingEvents();

        OutboxEventEntity updated = reload(event);
        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(updated.getPublishedAt()).isNotNull();
        assertThat(updated.getLastError()).isNull();
        verify(kafkaTemplate).send(TOPIC, event.getEventId().toString(), event.getPayload());
    }

    @Test
    void futureNextAttemptEventIsSkipped() {
        OutboxEventEntity event = saveEvent(OutboxStatus.PENDING, 0, LocalDateTime.now().plusHours(1));

        outboxService.processPendingEvents();

        assertThat(reload(event).getStatus()).isEqualTo(OutboxStatus.PENDING);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void failedPublishIncrementsRetryCountAndSchedulesLaterAttempt() {
        OutboxEventEntity event = saveEvent(OutboxStatus.PENDING, 0, LocalDateTime.now().minusSeconds(1));
        LocalDateTime processingStartedAt = LocalDateTime.now();
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker unavailable")));

        outboxService.processPendingEvents();

        OutboxEventEntity updated = reload(event);
        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(updated.getRetryCount()).isEqualTo(1);
        assertThat(updated.getNextAttemptAt()).isAfter(processingStartedAt);
        assertThat(updated.getLastError()).isEqualTo("broker unavailable");
    }

    @Test
    void eventBecomesFailedAfterMaxRetries() {
        OutboxEventEntity event = saveEvent(OutboxStatus.PENDING, 2, LocalDateTime.now().minusSeconds(1));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("still unavailable")));

        outboxService.processPendingEvents();

        OutboxEventEntity updated = reload(event);
        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(updated.getRetryCount()).isEqualTo(3);
        assertThat(updated.getLastError()).isEqualTo("still unavailable");
    }

    @Test
    void failedEventDoesNotStopLaterEventsInBatch() {
        OutboxEventEntity failing = saveEvent(
                OutboxStatus.PENDING,
                0,
                LocalDateTime.now().minusSeconds(2)
        );
        OutboxEventEntity succeeding = saveEvent(
                OutboxStatus.PENDING,
                0,
                LocalDateTime.now().minusSeconds(1)
        );
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(1);
                    if (failing.getEventId().toString().equals(key)) {
                        return CompletableFuture.failedFuture(
                                new IllegalStateException("first publish failed")
                        );
                    }
                    return CompletableFuture.completedFuture(null);
                });

        outboxService.processPendingEvents();

        assertThat(reload(failing).getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(reload(failing).getRetryCount()).isEqualTo(1);
        assertThat(reload(succeeding).getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        verify(kafkaTemplate).send(TOPIC, succeeding.getEventId().toString(), succeeding.getPayload());
    }

    @Test
    void publishedEventsAreNotSelectedAgain() {
        OutboxEventEntity event = saveEvent(
                OutboxStatus.PUBLISHED,
                0,
                LocalDateTime.now().minusSeconds(1)
        );
        event.setPublishedAt(LocalDateTime.now().minusSeconds(1));
        outboxEventRepository.flush();

        outboxService.processPendingEvents();

        assertThat(reload(event).getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    private OutboxEventEntity saveEvent(
            OutboxStatus status,
            int retryCount,
            LocalDateTime nextAttemptAt
    ) {
        return outboxEventRepository.saveAndFlush(
                OutboxEventEntity.builder()
                        .eventId(UUID.randomUUID())
                        .eventType("TRANSFER_COMPLETED")
                        .aggregateType("TRANSACTION")
                        .aggregateId(100L)
                        .payload("{\"transactionId\":100}")
                        .status(status)
                        .retryCount(retryCount)
                        .nextAttemptAt(nextAttemptAt)
                        .build()
        );
    }

    private OutboxEventEntity reload(OutboxEventEntity event) {
        outboxEventRepository.flush();
        return outboxEventRepository.findById(event.getId()).orElseThrow();
    }
}
