package com.example.payflow.payment.dto;

import com.example.payflow.payment.enums.CurrencyEnum;
import com.example.payflow.payment.enums.TransactionDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerResponse {
    private Long id;
    private Long transactionId;
    private Long walletId;
    private TransactionDirection transactionDirection;
    private BigDecimal amount;
    private CurrencyEnum currency;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
}
