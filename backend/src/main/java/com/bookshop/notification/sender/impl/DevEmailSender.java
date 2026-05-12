package com.bookshop.notification.sender.impl;

import com.bookshop.notification.sender.EmailSender;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
@Service
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("DEV MODE: Preparing to send HTML email via Mailtrap to {}", to);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom("noreply@bookstacks.store");
            helper.setTo(to);
            helper.setSubject(subject);

            helper.setText(body, true);

            mailSender.send(mimeMessage);
            log.info("DEV MODE: Successfully sent HTML email to {}", to);

        } catch (Exception e) {
            log.error("DEV MODE: Failed to send HTML email to {}", to, e);
        }
    }
}
