package com.example.payflow_rewrite.Wallet.Controller;

import com.example.payflow_rewrite.Wallet.Dto.CreateWalletRequest;
import com.example.payflow_rewrite.Wallet.Dto.WalletResponse;
import com.example.payflow_rewrite.Wallet.Service.WalletService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @Transactional
    @PostMapping("create")
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest createWalletRequest){
        WalletResponse response = walletService.createWallet(createWalletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
