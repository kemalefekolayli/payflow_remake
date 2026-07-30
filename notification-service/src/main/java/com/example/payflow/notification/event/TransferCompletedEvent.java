package com.example.payflow.notification.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferCompletedEvent(
        @NotNull UUID eventId,
        @NotNull Long transactionId,
        @NotBlank String transactionRef,
        @NotNull Long senderWalletId,
        @NotNull Long receiverWalletId,
        @NotNull Long senderUserId,
        @NotNull @Positive BigDecimal amount,
        @NotNull Currency currency,
        @NotNull LocalDateTime occurredAt
) {
}
