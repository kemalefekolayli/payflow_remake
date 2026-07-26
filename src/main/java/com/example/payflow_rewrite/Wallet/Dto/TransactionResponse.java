package com.example.payflow_rewrite.Wallet.Dto;



import com.example.payflow_rewrite.Wallet.Enums.TransactionStatus;
import com.example.payflow_rewrite.Wallet.Enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private String transactionRef;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private TransactionStatus status;
    private String description;
    private Long senderWalletId;
    private Long receiverWalletId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}