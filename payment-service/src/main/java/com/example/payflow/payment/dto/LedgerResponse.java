package com.example.payflow.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerResponse {

    private Long creditEntryId;
    private Long debitEntryId;
    private Boolean ledgerSaveStatus;

}
