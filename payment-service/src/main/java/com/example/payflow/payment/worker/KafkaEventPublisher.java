package com.example.payflow.payment.worker;

import com.example.payflow.payment.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private static final String TRANSFER_COMPLETED = "TRANSFER_COMPLETED";
    private static final String TRANSFER_COMPLETED_TOPIC = "payment.transfer-completed";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(OutboxEventEntity event) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(
                resolveTopic(event.getEventType()),
                event.getEventId().toString(),
                event.getPayload()
        ).get();
    }

    private String resolveTopic(String eventType) {
        if (TRANSFER_COMPLETED.equals(eventType)) {
            return TRANSFER_COMPLETED_TOPIC;
        }
        throw new IllegalArgumentException("No Kafka topic configured for event type: " + eventType);
    }
}
