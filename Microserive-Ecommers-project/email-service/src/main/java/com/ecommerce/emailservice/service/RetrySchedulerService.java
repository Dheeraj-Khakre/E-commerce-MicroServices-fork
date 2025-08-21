package com.ecommerce.emailservice.service;

import com.ecommerce.emailservice.autoclaim.EmailClaimService;
import com.ecommerce.emailservice.models.EmailLog;
import com.ecommerce.emailservice.models.EmailStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetrySchedulerService {

    private final EmailClaimService emailClaimService;
    private final EmailService EmailService;

    // Control how many emails to process in one scheduler run to limit batch size
    private static final int MAX_PROCESS_PER_RUN = 100;
    private static final int MAX_RETRIES = 5;

    /**
     * Scheduler runs every minute (configurable). It repeatedly claims documents and processes them.
     */
    @Scheduled(fixedDelayString = "${email.retry.fixed-delay-ms:6000}")
    public void retryFailedEmails() {
        int processed = 0;
        while (processed < MAX_PROCESS_PER_RUN) {
            EmailLog claimed = emailClaimService.claimOneForProcessing();
            if (claimed == null) {
                // nothing left to process
                break;
            }
            try {
                log.info("Processing claimed email id={} recipient={} createdAt={} nextAttemptAt={}",
                        claimed.getId(), claimed.getRecipient(), claimed.getCreatedAt(), claimed.getNextAttemptAt());

                boolean sent = EmailService.sendEmail(claimed);

                if (!sent) {
                    // On failure, check if exceeded allowed retries and mark permanent if so
                    int retries = claimed.getRetryCount() == null ? 0 : claimed.getRetryCount();
                    if (retries >= MAX_RETRIES) {
                        EmailService.markPermanentlyFailed(claimed);
                    }
                }

            } catch (Exception ex) {
                // unexpected exception: ensure record is put back to FAILED with nextAttemptAt set
                log.error("Unexpected error processing email id={}: {}", claimed.getId(), ex.getMessage(), ex);
                // set nextAttemptAt to somewhat later to avoid hot loops
                claimed.setStatus(EmailStatus.FAILED);
                claimed.setLastError("Scheduler unexpected error: " + ex.getMessage());
                claimed.setNextAttemptAt(Instant.now().plusSeconds(60 * 10)); // retry after 10 minutes
                // persistence will be handled by SimpleEmailService or you can autowire repo and save here
            }

            processed++;
        }
        log.debug("RetrySchedulerService run finished at {} processed={}", Instant.now(), processed);
    }
}
