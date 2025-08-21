package com.ecommerce.emailservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailOutbox {

    private String id;
    private String aggregateId;
    private String eventType;
    private String recipient;
    private String subject;
    private String template;
    private Map<String, Object> variables;
    private String body;       // optional: pre-rendered or for audit
    private boolean published;
    private int retryCount;
    private Instant createdAt;
    private Instant publishedAt;
}
