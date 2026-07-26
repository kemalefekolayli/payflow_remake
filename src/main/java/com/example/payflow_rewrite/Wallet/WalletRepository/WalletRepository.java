package com.example.payflow_rewrite.Wallet.WalletRepository;

import com.example.payflow_rewrite.Wallet.Entity.WalletEntity;
import com.example.payflow_rewrite.Wallet.Enums.CurrencyEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    Boolean existsByUserIdAndCurrency(Long userId, CurrencyEnum cur);

    Optional<WalletEntity> findByUserIdAndId(Long userId, Long walletId);

    List<WalletEntity> findAllByUserId(Long userId);
}
