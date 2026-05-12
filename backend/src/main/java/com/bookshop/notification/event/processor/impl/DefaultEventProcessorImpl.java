package com.bookshop.notification.event.processor.impl;

import com.bookshop.notification.event.processor.EventProcessor;
import com.bookshop.notification.sender.EmailSender;
import com.bookshop.shared.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultEventProcessorImpl implements EventProcessor {
    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;
    
    @Override
    public void process(NotificationEvent event) {
        log.info("Processing notification event: {}", event.eventId());
        if ("ACCOUNT_VERIFICATION".equalsIgnoreCase(event.eventType())) {
            log.info("Sending Verification Email to: {}", event.recipient());
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(event.payload());
            String htmlBody = templateEngine.process("emails/verification", thymeleafContext);

            // Send the hydrated HTML string!
            emailSender.sendEmail(event.recipient(), "Verify your BookStacks Account", htmlBody);

        } else if ("PASSWORD_RESET".equalsIgnoreCase(event.eventType())) {
            log.info("Sending Password Reset Email to: {}", event.recipient());

            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(event.payload());

            String htmlBody = templateEngine.process("emails/password-reset", thymeleafContext);

            emailSender.sendEmail(event.recipient(), "Reset your BookStacks Password", htmlBody);
        } else {
            log.warn("Unknown event type received: {}", event.eventType());
        }
    }
}
