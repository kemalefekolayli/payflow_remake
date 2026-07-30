package com.example.paytrans.notification.email;

public interface EmailSender {

    void send(String recipient, String subject, String body) throws Exception;
}
