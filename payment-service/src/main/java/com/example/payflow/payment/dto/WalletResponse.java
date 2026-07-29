package com.example.payflow.payment.dto;


import com.example.payflow.payment.enums.CurrencyEnum;
import com.example.payflow.payment.enums.WalletStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private CurrencyEnum currency;
    private WalletStatus status;
    private LocalDateTime createdAt;
}
