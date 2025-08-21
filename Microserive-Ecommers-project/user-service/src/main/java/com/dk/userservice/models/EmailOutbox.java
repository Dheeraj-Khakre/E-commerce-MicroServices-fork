package com.dk.userservice.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "email_outbox")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailOutbox {

    @Id
    private String id;

    // Unique identifier of the entity that triggered the email (e.g., userId, orderId)
    private String aggregateId;

    // Event type e.g. USER_CREATED, ORDER_PLACED, PASSWORD_RESET
    private String eventType;

    // Recipient email address
    private String recipient;

    // Email subject
    private String subject;

    // Template identifier (e.g., WELCOME_TEMPLATE)
    private String template;

    // Dynamic variables for the template
    private Map<String, Object> variables;

    // Raw email body (optional, for auditing/debugging)
    private String body;

    // Published flag for Kafka
    private boolean published;

    // Retry attempts
    private int retryCount;

    // Audit fields
    @CreatedDate
    private Instant createdAt;

    private Instant publishedAt;
}
