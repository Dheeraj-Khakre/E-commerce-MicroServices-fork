package com.dk.userservice.kafkaservice;

import com.dk.userservice.external.EmailServiceClient;
import com.dk.userservice.models.EmailOutbox;
import com.dk.userservice.repository.EmailOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailPublisherScheduler {

    private final EmailOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final EmailServiceClient emailServiceClient; // Feign client

    @Scheduled(fixedDelay = 2000)
    @CircuitBreaker(name = "email", fallbackMethod = "fallbackPublish")
    @Retry(name = "email")
  //  @TimeLimiter(name = "email")
    public void publish() throws Exception {
        List<EmailOutbox> rows =
                outboxRepository.findTop50ByPublishedFalseOrderByCreatedAtAsc();

        for (EmailOutbox row : rows) {
            String payload = objectMapper.writeValueAsString(row);

            // try publishing to Kafka
            kafkaTemplate.send("emails", row.getAggregateId(), payload).get(5, TimeUnit.SECONDS);

            row.setPublished(true);
            row.setPublishedAt(Instant.now());
            outboxRepository.save(row);
        }
    }

    /**
     * Fallback method when CircuitBreaker is OPEN or publish() fails.
     * Instead of Kafka, we send directly using Feign client (email-service).
     */
    public void fallbackPublish(Throwable ex) {
        log.warn("Fallback triggered due to: {}", ex.getMessage());

        List<EmailOutbox> rows =
                outboxRepository.findTop50ByPublishedFalseOrderByCreatedAtAsc();

        for (EmailOutbox row : rows) {
            try {
                ResponseEntity<EmailOutbox> response = emailServiceClient.createEmail(row);

                if (response.getStatusCode().is2xxSuccessful()) {
                    row.setPublished(true);
                    row.setPublishedAt(Instant.now());
                    outboxRepository.save(row);
                    log.info("Fallback: Email sent via EmailService for id={}", row.getId());
                } else {
                    row.setRetryCount(row.getRetryCount() + 1);
                    outboxRepository.save(row);
                    log.error("Fallback failed: EmailService did not accept email {}", row.getId());
                }

            } catch (Exception e) {
                row.setRetryCount(row.getRetryCount() + 1);
                outboxRepository.save(row);
                log.error("Fallback also failed for email {} due to {}", row.getId(), e.getMessage());
            }
        }
    }
}
