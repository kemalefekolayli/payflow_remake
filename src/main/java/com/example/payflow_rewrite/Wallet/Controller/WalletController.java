package com.example.payflow_rewrite.Wallet.Controller;

import com.example.payflow_rewrite.Auth.Controller.AuthController;
import com.example.payflow_rewrite.Auth.Dto.UserResponse;
import com.example.payflow_rewrite.Auth.Service.AuthService;
import com.example.payflow_rewrite.Wallet.Dto.CreateWalletRequest;
import com.example.payflow_rewrite.Wallet.Dto.WalletResponse;
import com.example.payflow_rewrite.Wallet.Service.WalletService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final AuthService authService;

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest createWalletRequest){
        WalletResponse response = walletService.createWallet(createWalletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{walletId}") //return a wallet by : walletid,userid,
    public ResponseEntity<WalletResponse> getWalletDetails(@PathVariable Long walletId , Authentication auth ){
        UserResponse userResponse = authService.getProfile(auth.getName());
        WalletResponse walletResponse = walletService.getWalletDetail(userResponse.getId(),walletId);
        return ResponseEntity.ok(walletResponse);
    }

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getUserWallets(Authentication auth) {
        UserResponse userResponse = authService.getProfile(auth.getName());
        List<WalletResponse> wallets = walletService.getWalletsByUserId(userResponse.getId());
        return ResponseEntity.ok(wallets);
    }

}
