package com.example.payflow_rewrite.Wallet.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @Builder.Default
    private String currency = "TL";

}
