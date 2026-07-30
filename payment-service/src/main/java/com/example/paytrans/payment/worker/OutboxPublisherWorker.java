package com.example.paytrans.payment.worker;

import com.example.paytrans.payment.service.OutboxService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "outbox.worker.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxPublisherWorker {

    private final OutboxService outboxService;

    @Scheduled(fixedDelayString = "${outbox.worker.delay-ms:1000}")
    public void publishPendingEvents() {
        outboxService.processPendingEvents();
    }
}
