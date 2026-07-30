package com.example.payflow.notification;

import com.example.payflow.notification.email.EmailSender;
import com.example.payflow.notification.entity.NotificationChannel;
import com.example.payflow.notification.entity.NotificationEntity;
import com.example.payflow.notification.entity.NotificationStatus;
import com.example.payflow.notification.event.Currency;
import com.example.payflow.notification.event.TransferCompletedEvent;
import com.example.payflow.notification.repository.NotificationRepository;
import com.example.payflow.notification.repository.ProcessedEventRepository;
import com.example.payflow.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        classes = NotificationServiceApplication.class,
        properties = "spring.datasource.url=jdbc:h2:mem:notification_service_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
)
class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @MockitoBean
    private EmailSender emailSender;

    @BeforeEach
    void cleanDatabase() {
        notificationRepository.deleteAll();
        processedEventRepository.deleteAll();
    }

    @Test
    void firstEventCreatesOneNotificationAndProcessedEvent() {
        TransferCompletedEvent event = event(10L);

        notificationService.handleTransferCompleted(event);

        assertThat(notificationRepository.count()).isEqualTo(1);
        assertThat(processedEventRepository.count()).isEqualTo(1);

        NotificationEntity notification = notificationRepository.findAll().get(0);
        assertThat(notification.getEventId()).isEqualTo(event.eventId());
        assertThat(notification.getUserId()).isEqualTo(10L);
        assertThat(notification.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(notification.getRecipient()).isEqualTo("user-10@payflow.local");
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getRetryCount()).isZero();
    }

    @Test
    void duplicateEventCreatesNoSecondNotification() {
        TransferCompletedEvent event = event(10L);

        notificationService.handleTransferCompleted(event);
        notificationService.handleTransferCompleted(event);

        assertThat(notificationRepository.count()).isEqualTo(1);
        assertThat(processedEventRepository.count()).isEqualTo(1);
    }

    @Test
    void successfulDeliveryMarksNotificationSent() throws Exception {
        NotificationEntity notification = saveReadyNotification("success@payflow.local", 0);

        notificationService.processPendingNotifications();

        NotificationEntity updated = reload(notification);
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(updated.getSentAt()).isNotNull();
        assertThat(updated.getLastError()).isNull();
        verify(emailSender).send(
                notification.getRecipient(),
                notification.getSubject(),
                notification.getBody()
        );
    }

    @Test
    void failedDeliverySchedulesRetry() throws Exception {
        NotificationEntity notification = saveReadyNotification("retry@payflow.local", 0);
        LocalDateTime processingStartedAt = LocalDateTime.now();
        doThrow(new IllegalStateException("email unavailable"))
                .when(emailSender)
                .send(anyString(), anyString(), anyString());

        notificationService.processPendingNotifications();

        NotificationEntity updated = reload(notification);
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(updated.getRetryCount()).isEqualTo(1);
        assertThat(updated.getNextAttemptAt()).isAfter(processingStartedAt);
        assertThat(updated.getLastError()).isEqualTo("email unavailable");
    }

    @Test
    void maximumRetriesMarksNotificationFailed() throws Exception {
        NotificationEntity notification = saveReadyNotification("failed@payflow.local", 2);
        doThrow(new IllegalStateException("still unavailable"))
                .when(emailSender)
                .send(anyString(), anyString(), anyString());

        notificationService.processPendingNotifications();

        NotificationEntity updated = reload(notification);
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(updated.getRetryCount()).isEqualTo(3);
        assertThat(updated.getLastError()).isEqualTo("still unavailable");
    }

    @Test
    void oneFailureDoesNotStopLaterNotificationInBatch() throws Exception {
        NotificationEntity failing = saveReadyNotification("first@payflow.local", 0);
        NotificationEntity succeeding = saveReadyNotification("second@payflow.local", 0);
        doAnswer(invocation -> {
            if (failing.getRecipient().equals(invocation.getArgument(0))) {
                throw new IllegalStateException("first email failed");
            }
            return null;
        }).when(emailSender).send(anyString(), anyString(), anyString());

        notificationService.processPendingNotifications();

        assertThat(reload(failing).getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(reload(failing).getRetryCount()).isEqualTo(1);
        assertThat(reload(succeeding).getStatus()).isEqualTo(NotificationStatus.SENT);
        verify(emailSender).send(
                succeeding.getRecipient(),
                succeeding.getSubject(),
                succeeding.getBody()
        );
    }

    @Test
    void persistenceFailureRollsBackNotificationAndProcessedEvent() {
        TransferCompletedEvent invalidEvent = new TransferCompletedEvent(
                UUID.randomUUID(),
                100L,
                "TX-ROLLBACK",
                1L,
                2L,
                null,
                new BigDecimal("25.00"),
                Currency.TL,
                LocalDateTime.now()
        );

        assertThatThrownBy(() -> notificationService.handleTransferCompleted(invalidEvent))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(notificationRepository.count()).isZero();
        assertThat(processedEventRepository.count()).isZero();
    }

    private TransferCompletedEvent event(Long userId) {
        return new TransferCompletedEvent(
                UUID.randomUUID(),
                100L,
                "TX-100",
                1L,
                2L,
                userId,
                new BigDecimal("25.00"),
                Currency.TL,
                LocalDateTime.now()
        );
    }

    private NotificationEntity saveReadyNotification(String recipient, int retryCount) {
        return notificationRepository.saveAndFlush(
                NotificationEntity.builder()
                        .eventId(UUID.randomUUID())
                        .userId(10L)
                        .channel(NotificationChannel.EMAIL)
                        .recipient(recipient)
                        .subject("Transfer completed")
                        .body("Transfer completed.")
                        .status(NotificationStatus.PENDING)
                        .retryCount(retryCount)
                        .nextAttemptAt(LocalDateTime.now().minusSeconds(1))
                        .build()
        );
    }

    private NotificationEntity reload(NotificationEntity notification) {
        return notificationRepository.findById(notification.getId()).orElseThrow();
    }
}
