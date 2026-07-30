package com.example.paytrans.payment.repository;

import com.example.paytrans.payment.entity.LedgerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntity, Long> {
    Page<LedgerEntity> findByWalletId(Long walletId, Pageable pageable);

    List<LedgerEntity> findByTransactionIdOrderByIdAsc(Long transactionId);
}
