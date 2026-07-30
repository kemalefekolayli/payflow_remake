package com.example.payflow.notification.repository;

import com.example.payflow.notification.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, Long> {

    boolean existsByEventId(UUID eventId);
}
