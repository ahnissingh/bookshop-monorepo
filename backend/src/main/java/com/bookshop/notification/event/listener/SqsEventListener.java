package com.bookshop.notification.event.listener;

import com.bookshop.notification.event.processor.EventProcessor;
import com.bookshop.shared.event.NotificationEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * TODO:  Move this logic to AWS Lambda!
 * Currently listening on the EC2
 * In the future, this class should be deleted, and a separate Serverless
 * Lambda function  should trigger on this SQS queue
 * to handle Thymeleaf rendering and SES email sending independently.
 */
@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class SqsEventListener {

    private final EventProcessor eventProcessor;

    //Long Polling (SQS configured 20secs)
    @SqsListener("${app.aws.sqs.notification-queue-url}")
    public void listenToSqsQueue(NotificationEvent event) {
        log.info("AWS SQS: Picked up message from queue. Passing to processor. Event ID: {}", event.eventId());

        eventProcessor.process(event);
    }
}