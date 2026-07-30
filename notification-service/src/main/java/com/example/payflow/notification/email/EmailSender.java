package com.example.payflow.notification.email;

public interface EmailSender {

    void send(String recipient, String subject, String body) throws Exception;
}
