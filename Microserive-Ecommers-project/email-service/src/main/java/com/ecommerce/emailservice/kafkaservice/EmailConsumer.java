package com.ecommerce.emailservice.kafkaservice;

import com.ecommerce.emailservice.service.SimpleEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailConsumer {

    private final SimpleEmailService service;
    @KafkaListener(topics = "user-topic", groupId = "email-service-group")
    public void consume(String message,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("📧 Email service received user event: {}, key = {}", message, key);

        boolean isSent = service.sendEmail(
                "khakredheeraj@gmail.com",
                "Kafka Test Email",
                "Kafka from docker is working fine ..." + message
        );

    }

}