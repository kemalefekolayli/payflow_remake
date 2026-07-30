package com.example.paytrans.notification.listener;

import com.example.paytrans.notification.event.TransferCompletedEvent;
import com.example.paytrans.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@RequiredArgsConstructor
public class TransferCompletedListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${notification.kafka.transfer-completed-topic}")
    public void consume(@Valid TransferCompletedEvent event) {
        notificationService.handleTransferCompleted(event);
    }
}
