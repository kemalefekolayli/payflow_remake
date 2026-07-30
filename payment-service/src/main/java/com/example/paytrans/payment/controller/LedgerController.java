package com.example.paytrans.payment.controller;

import com.example.paytrans.payment.dto.LedgerResponse;
import com.example.paytrans.payment.security.AuthenticatedUserPrincipal;
import com.example.paytrans.payment.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/api/wallets/{walletId}/ledger")
    public ResponseEntity<Page<LedgerResponse>> getWalletLedger(
            @PathVariable Long walletId,
            Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ResponseEntity.ok(ledgerService.getWalletLedger(walletId, principal.userId(), pageable));
    }

    @GetMapping("/api/ledger/transactions/{transactionId}")
    public ResponseEntity<List<LedgerResponse>> getTransactionLedger(
            @PathVariable Long transactionId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ResponseEntity.ok(
                ledgerService.getTransactionLedger(transactionId, principal.userId())
        );
    }
}
