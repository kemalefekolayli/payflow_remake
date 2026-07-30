package com.example.payflow.notification;

import com.example.payflow.notification.event.Currency;
import com.example.payflow.notification.event.TransferCompletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransferCompletedEventContractTest {

    @Test
    void paymentTransferCompletedPayloadDeserializesIntoLocalContract() {
        String payload = """
                {
                  "eventId": "550e8400-e29b-41d4-a716-446655440000",
                  "transactionId": 100,
                  "transactionRef": "TX-100",
                  "senderWalletId": 1,
                  "receiverWalletId": 2,
                  "senderUserId": 10,
                  "amount": 25.00,
                  "currency": "TL",
                  "occurredAt": "2026-07-30T20:00:00"
                }
                """;

        try (JacksonJsonDeserializer<TransferCompletedEvent> deserializer =
                     new JacksonJsonDeserializer<>(TransferCompletedEvent.class)) {
            TransferCompletedEvent event = deserializer.deserialize(
                    "payment.transfer-completed",
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            assertThat(event.eventId())
                    .isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
            assertThat(event.transactionId()).isEqualTo(100L);
            assertThat(event.transactionRef()).isEqualTo("TX-100");
            assertThat(event.senderWalletId()).isEqualTo(1L);
            assertThat(event.receiverWalletId()).isEqualTo(2L);
            assertThat(event.senderUserId()).isEqualTo(10L);
            assertThat(event.amount()).isEqualByComparingTo("25.00");
            assertThat(event.currency()).isEqualTo(Currency.TL);
            assertThat(event.occurredAt()).isEqualTo(LocalDateTime.of(2026, 7, 30, 20, 0));
        }
    }
}
