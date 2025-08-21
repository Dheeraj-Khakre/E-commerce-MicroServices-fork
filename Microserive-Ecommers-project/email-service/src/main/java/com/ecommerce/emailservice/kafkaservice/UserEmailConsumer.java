package com.ecommerce.emailservice.kafkaservice;

import com.ecommerce.emailservice.models.EmailOutbox;
import com.ecommerce.emailservice.service.SimpleEmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEmailConsumer {
    private final SpringTemplateEngine templateEngine;
   private final SimpleEmailService service;
    private final ObjectMapper objectMapper;



    @KafkaListener(topics = "emails", groupId = "email-service-group")
    public void listen(String message) {
        try {
            // parse payload into object (adjust if your payload shape differs)
            EmailOutbox outbox = objectMapper.readValue(message, EmailOutbox.class);

            // 1) Render the template to HTML
            Context ctx = new Context();
            // pass variables map to Thymeleaf
            if (outbox.getVariables() != null) {
                ctx.setVariables(outbox.getVariables());
            }
            // optionally add or override variables (like tracking IDs)
            ctx.setVariable("sentAt", Instant.now().toString());

            String html = templateEngine.process(outbox.getTemplate(), ctx);

            // 2) Send email (HTML)
         service.sendEmail(outbox.getRecipient(), outbox.getSubject(), html);

            log.info("Email sent to {}", outbox.getRecipient());

        } catch (Exception e) {
            log.error("Failed to process email message", e);
            // increment retryCount / update outbox in Mongo if required
            // optionally publish to DLQ
        }
    }
}
