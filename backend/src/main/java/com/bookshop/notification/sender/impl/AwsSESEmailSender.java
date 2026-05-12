package com.bookshop.notification.sender.impl;

import com.bookshop.notification.sender.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.email.provider", havingValue = "ses", matchIfMissing = true)
public class AwsSESEmailSender implements EmailSender {
    private final SesClient sesClient;

    @Value("${app.aws.ses.from-email}")
    private String fromEmail;

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("PROD MODE: Sending real email via Amazon SES to {}", to);

        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .html(Content.builder().data(body).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build();

            SendEmailResponse response = sesClient.sendEmail(emailRequest);

            log.info(" PROD MODE: Successfully sent email to {}. SES Message ID: {}", to, response.messageId());

        } catch (SesException e) {
            log.error(" PROD MODE: AWS SES failed to send email to {}", to, e);
            throw new RuntimeException("Email sending failed via SES", e);
        }
    }
}