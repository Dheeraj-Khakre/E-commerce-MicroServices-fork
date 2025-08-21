package com.ecommerce.emailservice.models;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "emails")
public class EmailLog {
    @Id
    private String id;

    private String sender;             // e.g., noreply@ecommerce.com
    private String recipient;   // customer's email
    private List<String> cc;      // cc email
    private String subject;
    private String content;
    private Instant sentAt;

    private List<Tag> tags;          // Link email to specific order, if applicable
    private EmailType emailType;          // e.g., ORDER_CONFIRMATION, PROMOTION, PASSWORD_RESET

    private EmailStatus status;             // SENT, FAILED, RETRY_PENDING
    private Integer retryCount;        // If implementing retries
    private List<String> attachments;  // Optional: filenames or URLs to invoice, etc.

    private boolean isTransactional;   // Distinguish system vs. marketing messages

    private String lastError;

    @CreatedDate
    private Instant createdAt;
    private Instant nextAttemptAt;  // when to try next
    private Instant processingAt;   // when claimed
}