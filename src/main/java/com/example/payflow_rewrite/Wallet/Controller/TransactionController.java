package com.example.payflow_rewrite.Wallet.Controller;


import com.example.payflow_rewrite.Auth.Dto.UserResponse;
import com.example.payflow_rewrite.Auth.Service.AuthService;
import com.example.payflow_rewrite.Wallet.Dto.AddMoneyRequest;
import com.example.payflow_rewrite.Wallet.Dto.SendMoneyRequest;
import com.example.payflow_rewrite.Wallet.Dto.TransactionResponse;
import com.example.payflow_rewrite.Wallet.Dto.WalletResponse;
import com.example.payflow_rewrite.Wallet.Entity.TransactionEntity;
import com.example.payflow_rewrite.Wallet.Service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthService authService;

    @PostMapping("/{walletId}/add-money")
    public ResponseEntity<TransactionResponse> addMoney(@Valid @RequestBody AddMoneyRequest req, @PathVariable Long walletId, Authentication auth){
        UserResponse userResponse = authService.getProfile(auth.getName());
        TransactionResponse response = transactionService.addMoney(req,walletId, userResponse.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{walletId}/send-money")
    public ResponseEntity<TransactionResponse> sendMoney(@Valid @RequestBody SendMoneyRequest req, @PathVariable Long walletId, Authentication auth){
        UserResponse userResponse = authService.getProfile(auth.getName());
        TransactionResponse response = transactionService.sendMoney(req,walletId, userResponse.getId());
        return ResponseEntity.ok(response);
    }
}
