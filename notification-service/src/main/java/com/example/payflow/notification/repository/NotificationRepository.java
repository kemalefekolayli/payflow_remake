package com.example.payflow.notification.repository;

import com.example.payflow.notification.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @Query(value = """
            SELECT *
            FROM notifications
            WHERE status = 'PENDING'
              AND next_attempt_at <= CURRENT_TIMESTAMP
            ORDER BY created_at ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<NotificationEntity> findReadyBatchForUpdate(@Param("batchSize") int batchSize);
}
