package com.example.paytrans.notification.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String recipient, String subject, String body) {
        log.info(
                "Sending logging email recipient={}, subject={}, body={}",
                recipient,
                subject,
                body
        );
    }
}
