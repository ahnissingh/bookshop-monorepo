package com.bookshop.notification.event.publisher.impl;

import com.bookshop.notification.event.publisher.EventPublisher;
import com.bookshop.shared.event.NotificationEvent;


import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class ProdEventPublisher implements EventPublisher {
    private final SqsTemplate sqsTemplate;
    @Value("${app.aws.sqs.notification-queue-url}")
    private String queueUrl;
    @Override
    public void publish(String topicName, NotificationEvent event) {
        log.info("PROD MODE: Publishing event {} to Real AWS SQS Topic [{}]", event.eventId(), topicName);
        sqsTemplate.send(queueUrl,event);
    }
}