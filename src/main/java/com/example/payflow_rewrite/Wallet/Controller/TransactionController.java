package com.example.payflow_rewrite.Wallet.Controller;


import com.example.payflow_rewrite.Auth.Dto.UserResponse;
import com.example.payflow_rewrite.Auth.Service.AuthService;
import com.example.payflow_rewrite.Wallet.Dto.*;
import com.example.payflow_rewrite.Wallet.Entity.TransactionEntity;
import com.example.payflow_rewrite.Wallet.Service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    @GetMapping("/wallets/{walletId}/history")
    public ResponseEntity<PagedResponse<TransactionResponse>> getTransactionHistory(@PathVariable Long walletId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(transactionService.getTransactionHistory(walletId, pageable));
    }

}
