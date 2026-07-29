package com.example.payflow.payment.repository;

import com.example.payflow.payment.entity.WalletEntity;
import com.example.payflow.payment.enums.CurrencyEnum;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    Boolean existsByUserIdAndCurrency(Long userId, CurrencyEnum cur);

    Optional<WalletEntity> findByUserIdAndId(Long userId, Long walletId);

    List<WalletEntity> findAllByUserId(Long userId);

    Optional<WalletEntity> findById(Long walletId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletEntity w WHERE w.id = :id")
    Optional<WalletEntity> findByIdWithLock(@Param("id") Long id);
}
