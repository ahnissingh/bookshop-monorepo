package com.bookshop.notification.event.processor;
import com.bookshop.shared.event.NotificationEvent;

public interface EventProcessor {
    void process(NotificationEvent event);
}