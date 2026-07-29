package com.example.payflow.payment.service;

import com.example.payflow.payment.entity.OutboxEventEntity;
import com.example.payflow.payment.enums.OutboxStatus;
import com.example.payflow.payment.event.TransferCompletedEvent;
import com.example.payflow.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private static final String TRANSFER_COMPLETED = "TRANSFER_COMPLETED";
    private static final String TRANSACTION = "TRANSACTION";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEventEntity createTransferCompletedEvent(TransferCompletedEvent event) {
        String payload = objectMapper.writeValueAsString(event);

        OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                .eventId(event.eventId())
                .eventType(TRANSFER_COMPLETED)
                .aggregateType(TRANSACTION)
                .aggregateId(event.transactionId())
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();

        return outboxEventRepository.save(outboxEvent);
    }
}
