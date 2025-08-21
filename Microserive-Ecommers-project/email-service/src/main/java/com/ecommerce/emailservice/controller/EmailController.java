package com.ecommerce.emailservice.controller;

import com.ecommerce.emailservice.models.EmailLog;
import com.ecommerce.emailservice.models.EmailOutbox;
import com.ecommerce.emailservice.service.EmailMidLayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailMidLayerService midLayerService;

    @PostMapping("/create")
    public ResponseEntity<EmailOutbox> createEmail(@RequestBody EmailOutbox emailOutbox) {
        EmailOutbox result = midLayerService.createEmail(emailOutbox);
        return ResponseEntity.ok(result);
    }
}
