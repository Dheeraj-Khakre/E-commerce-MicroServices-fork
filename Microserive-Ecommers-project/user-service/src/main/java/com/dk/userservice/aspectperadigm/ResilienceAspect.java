package com.dk.userservice.aspectperadigm;

import com.dk.userservice.external.EmailServiceClient;
import com.dk.userservice.models.EmailOutbox;
import com.dk.userservice.repository.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ResilienceAspect {

    private final EmailServiceClient emailServiceClient; // Feign client
    private final EmailOutboxRepository outboxRepository;

    @Around("@annotation(com.dk.userservice.kafkaservice.ResilientPublish)")
    public Object aroundPublish(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed(); // execute original method
        } catch (Exception e) {
            return handleFallback(e); // fallback on failure
        }
    }

    // Fallback logic if Kafka fails
    private Object handleFallback(Throwable ex) {
        log.warn("Fallback triggered due to {}", ex.getMessage());

        List<EmailOutbox> rows =
                outboxRepository.findTop50ByPublishedFalseOrderByCreatedAtAsc();

        for (EmailOutbox row : rows) {
            try {
                ResponseEntity<EmailOutbox> response = emailServiceClient.createEmail(row);
                if (response.getStatusCode().is2xxSuccessful()) {
                    row.setPublished(true);
                    row.setPublishedAt(Instant.now());
                    outboxRepository.save(row);
                    log.info("Fallback: Sent email via EmailService for id={}", row.getId());
                } else {
                    row.setRetryCount(row.getRetryCount() + 1);
                    outboxRepository.save(row);
                }
            } catch (Exception e) {
                row.setRetryCount(row.getRetryCount() + 1);
                outboxRepository.save(row);
                log.error("Fallback also failed for {}: {}", row.getId(), e.getMessage());
            }
        }
        return null;
    }
}
