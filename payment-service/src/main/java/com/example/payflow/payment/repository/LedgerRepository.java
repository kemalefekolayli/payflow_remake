package com.example.payflow.payment.repository;

import com.example.payflow.payment.entity.LedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntity, Long> {
}
