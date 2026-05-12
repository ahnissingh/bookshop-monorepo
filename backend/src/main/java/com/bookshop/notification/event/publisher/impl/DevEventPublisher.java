package com.bookshop.notification.event.publisher.impl;

import com.bookshop.notification.event.publisher.EventPublisher;
import com.bookshop.shared.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevEventPublisher implements EventPublisher {
    private final ApplicationEventPublisher springEventPublisher;

    @Override
    public void publish(String topicName, NotificationEvent event) {
        log.info("DEV MODE: Mock publishing event {} to virtual topic [{}]", event.eventId(), topicName);
        // We push it to Spring's internal event bus instead of AWS SNS , in Prod Implemenation of our interface we will do that
        springEventPublisher.publishEvent(event);


    }
}
