package com.bookshop.notification.event.publisher;

import com.bookshop.shared.event.NotificationEvent;

public interface EventPublisher {
    void publish(String topicName, NotificationEvent event);
}