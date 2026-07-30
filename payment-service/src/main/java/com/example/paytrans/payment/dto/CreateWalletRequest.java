package com.example.paytrans.payment.dto;

import com.example.paytrans.payment.enums.CurrencyEnum;
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
