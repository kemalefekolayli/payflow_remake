package com.example.payflow_rewrite.Wallet.WalletRepository;

import com.example.payflow_rewrite.Wallet.Entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    Optional<TransactionEntity> findByIdempotencyKey(String idempotencyKey);
}
