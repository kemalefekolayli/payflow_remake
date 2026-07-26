package com.example.payflow_rewrite.Wallet.Controller;


import com.example.payflow_rewrite.Wallet.Dto.AddMoneyRequest;
import com.example.payflow_rewrite.Wallet.Dto.SendMoneyRequest;
import com.example.payflow_rewrite.Wallet.Dto.TransactionResponse;
import com.example.payflow_rewrite.Wallet.Dto.WalletResponse;
import com.example.payflow_rewrite.Wallet.Entity.TransactionEntity;
import com.example.payflow_rewrite.Wallet.Service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/{walletId}/add-money")
    public ResponseEntity<TransactionResponse> addMoney(@Valid AddMoneyRequest req, @PathVariable Long walletId){
        TransactionResponse response = transactionService.addMoney(req,walletId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{walletId}/send-money")
    public ResponseEntity<TransactionResponse> sendMoney(@Valid SendMoneyRequest req, @PathVariable Long walletId){
        TransactionResponse response = transactionService.sendMoney(req,walletId);
        return ResponseEntity.ok(response);
    }
}
