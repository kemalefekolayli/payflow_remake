package com.example.payflow.payment.controller;

import com.example.payflow.payment.security.AuthenticatedUserPrincipal;
import com.example.payflow.payment.dto.CreateWalletRequest;
import com.example.payflow.payment.dto.WalletResponse;
import com.example.payflow.payment.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<WalletResponse> createWallet(
            @Valid @RequestBody CreateWalletRequest createWalletRequest,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ){
        WalletResponse response = walletService.createWallet(createWalletRequest, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{walletId}") //return a wallet by : walletid,userid,
    public ResponseEntity<WalletResponse> getWalletDetails(
            @PathVariable Long walletId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ){
        WalletResponse walletResponse = walletService.getWalletDetail(principal.userId(),walletId);
        return ResponseEntity.ok(walletResponse);
    }

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getUserWallets(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        List<WalletResponse> wallets = walletService.getWalletsByUserId(principal.userId());
        return ResponseEntity.ok(wallets);
    }

    @PutMapping("/freeze/{walletId}")
    public ResponseEntity<Boolean> freezeAccount(
            @PathVariable Long walletId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ){
        return ResponseEntity.ok(walletService.freezeWallet(walletId, principal.userId()));
    }

    @PutMapping("/unfreeze/{walletId}")
    public ResponseEntity<Boolean> unfreezeAccount(
            @PathVariable Long walletId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ){
        return ResponseEntity.ok(walletService.unfreezeWallet(walletId, principal.userId()));
    }

}
