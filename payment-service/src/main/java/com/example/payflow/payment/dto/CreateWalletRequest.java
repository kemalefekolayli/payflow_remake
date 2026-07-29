package com.example.payflow.payment.dto;

import com.example.payflow.payment.enums.CurrencyEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {

    private Long userId;

    @NotNull(message = "Currency needed to be specified")
    private CurrencyEnum currency;

}
