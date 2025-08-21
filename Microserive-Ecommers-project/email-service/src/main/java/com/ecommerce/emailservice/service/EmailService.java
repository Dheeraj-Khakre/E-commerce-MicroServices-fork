package com.ecommerce.emailservice.service;

import com.ecommerce.emailservice.models.EmailLog;
import com.ecommerce.emailservice.models.EmailStatus;
import com.ecommerce.emailservice.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailRepository emailRepository;

    // backoff schedule (minutes) for 1..N retries; index 0 => retry 1
    private static final long[] BACKOFF_MINUTES = new long[]{1, 5, 15, 60, 240};

    private static final int MAX_RETRIES = 5;

    /**
     * Send email using existing EmailLog record (claimed). This updates the record in DB with success/failure,
     * increments retryCount and sets nextAttemptAt when failed.
     *
     * @param logEntry - the claimed EmailLog document (status == PROCESSING)
     * @return true if sent, false otherwise
     */
    public boolean sendEmail(EmailLog logEntry) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(logEntry.getRecipient());
            helper.setSubject(logEntry.getSubject());
            helper.setFrom(logEntry.getSender() == null ? "no-reply@yourdomain.com" : logEntry.getSender());
            helper.setText(logEntry.getContent() == null ? "" : logEntry.getContent(), true);

            // send
            javaMailSender.send(message);

            // update DB - success
            logEntry.setStatus(EmailStatus.SENT);
            logEntry.setSentAt(Instant.now());
            logEntry.setLastError(null);
            // reset processingAt
            logEntry.setProcessingAt(null);
            emailRepository.save(logEntry);

            log.info("Email sent to {} (id={})", logEntry.getRecipient(), logEntry.getId());
            return true;

        } catch (MessagingException | RuntimeException e) {
            // update DB - failure, set nextAttemptAt using exponential/backoff
            int currentRetry = logEntry.getRetryCount() == null ? 0 : logEntry.getRetryCount();
            int newRetry = currentRetry + 1;
            logEntry.setRetryCount(newRetry);
            logEntry.setLastError(e.getMessage());
            logEntry.setStatus(EmailStatus.FAILED);
            logEntry.setProcessingAt(null);

            // compute next attempt
            logEntry.setNextAttemptAt(computeNextAttempt(newRetry));
            emailRepository.save(logEntry);

            log.warn("Failed to send email to {} (id={}) retryCount={} error={}",
                    logEntry.getRecipient(), logEntry.getId(), newRetry, e.getMessage(), e);
            return false;
        }
    }

    private Instant computeNextAttempt(int retryCount) {
        // retryCount starts at 1 for first retry
        int idx = Math.min(retryCount - 1, BACKOFF_MINUTES.length - 1);
        long minutes = BACKOFF_MINUTES[idx];
        return Instant.now().plusSeconds(minutes * 60);
    }

    public boolean markPermanentlyFailed(EmailLog logEntry) {
        logEntry.setStatus(EmailStatus.PERMANENTLY_FAILED);
        emailRepository.save(logEntry);
        log.warn("Email marked PERMANENTLY_FAILED id={} recipient={}", logEntry.getId(), logEntry.getRecipient());
        return true;
    }
}
