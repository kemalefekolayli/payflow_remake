package com.example.paytrans.payment.event;

import com.example.paytrans.payment.enums.CurrencyEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferCompletedEvent(
        UUID eventId,
        Long transactionId,
        String transactionRef,
        Long senderWalletId,
        Long receiverWalletId,
        Long senderUserId,
        BigDecimal amount,
        CurrencyEnum currency,
        LocalDateTime occurredAt
) {
}
