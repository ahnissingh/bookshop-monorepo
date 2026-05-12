package com.bookshop.notification.sender;

public interface EmailSender {
    void sendEmail(String to, String subject, String body);
}