package com.example.payflow.payment.dto;

import com.example.payflow.payment.enums.CurrencyEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadWalletRequest {

    @NotBlank(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Wallet ID is required")
    private Long walletId;

    @NotBlank(message = "Currency needed to be specified")
    private CurrencyEnum currency;

}