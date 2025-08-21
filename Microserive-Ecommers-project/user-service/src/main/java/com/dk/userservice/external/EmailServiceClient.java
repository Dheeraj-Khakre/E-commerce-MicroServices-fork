package com.dk.userservice.external;

import com.dk.userservice.models.EmailOutbox;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "EMAIL-SERVICE") // Eureka serviceId, no URL needed
public interface EmailServiceClient {

    @PostMapping("/api/v1/emails/create")
    ResponseEntity<EmailOutbox> createEmail(@RequestBody EmailOutbox emailOutbox);
}