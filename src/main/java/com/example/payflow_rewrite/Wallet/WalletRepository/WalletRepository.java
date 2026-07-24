package com.example.payflow_rewrite.Wallet.WalletRepository;

import com.example.payflow_rewrite.Wallet.Entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
}
