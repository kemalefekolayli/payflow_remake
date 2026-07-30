package com.example.paytrans.payment.dto;

import com.example.paytrans.payment.enums.CurrencyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryRequest {
    BigDecimal amount;
    Long senderWalletId;
    Long receiverWalletId;
    Long transactionId;
    CurrencyEnum currency;
    BigDecimal senderBalanceBefore;
    BigDecimal senderBalanceAfter;
    BigDecimal receiverBalanceBefore;
    BigDecimal receiverBalanceAfter;
}
