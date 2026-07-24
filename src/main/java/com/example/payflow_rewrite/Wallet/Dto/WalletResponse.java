package com.example.payflow_rewrite.Wallet.Dto;


import com.example.payflow_rewrite.Wallet.Enums.WalletStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private Long id;
    private String userId;
    private BigDecimal balance;
    private String currency;
    private WalletStatus status;
    private LocalDateTime createdAt;
}
