package com.ecommerce.emailservice.service;

import com.ecommerce.emailservice.models.EmailLog;
import com.ecommerce.emailservice.models.EmailOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailMidLayerService {

    private final SpringTemplateEngine templateEngine;
    private final SimpleEmailService service;

    public EmailOutbox createEmail(EmailOutbox outbox) {
        try{
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
        outbox.setPublished(true);
        return outbox;
        } catch (Exception e) {
        log.error("Failed to process email message", e);
        // increment retryCount / update outbox in Mongo if required
        // optionally publish to DLQ
            outbox.setPublished(false);
            return outbox;
       }
    }
}
