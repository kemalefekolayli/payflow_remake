package com.example.payflow_rewrite.Wallet.Entity;


import com.example.payflow_rewrite.Wallet.Enums.CurrencyEnum;
import com.example.payflow_rewrite.Wallet.Enums.TransactionStatus;
import com.example.payflow_rewrite.Wallet.Enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "transactions", indexes = {
        @Index(name = "idx_sender_wallet_id", columnList = "senderWalletId"),
        @Index(name = "idx_receiver_wallet_id", columnList = "receiverWalletId")
})

public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(precision = 19, scale = 4)
    private BigDecimal balanceBefore;

    @Column(precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private String description;

    private Long senderWalletId;

    private Long receiverWalletId;

    @Column(unique = true)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
