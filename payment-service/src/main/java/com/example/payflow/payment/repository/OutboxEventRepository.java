package com.example.payflow.payment.repository;

import com.example.payflow.payment.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {
}
