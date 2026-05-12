package com.bookshop.notification.event.listener;

import com.bookshop.notification.event.processor.EventProcessor;
import com.bookshop.shared.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevEventListener {
    private final EventProcessor eventProcessor;

    @Async
    @TransactionalEventListener
    public void listenToSpringEvent(NotificationEvent event) {
        log.info("DEV: Received internal Spring Event. Passing to processor. {}", event);
        eventProcessor.process(event);
    }
}
