package com.ecommerce.emailservice.service;

import com.ecommerce.emailservice.models.EmailLog;
import com.ecommerce.emailservice.models.EmailStatus;
import com.ecommerce.emailservice.models.EmailType;
import com.ecommerce.emailservice.models.Tag;
import com.ecommerce.emailservice.repository.EmailRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleEmailService {

    @Autowired
    private final JavaMailSender javaMailSender;
    private final EmailRepository emailRepository;

    public Boolean sendEmail(String to, String subject, String body) {
        EmailLog log = new EmailLog();
        log.setSender("noreply@yourdomain.com");           // Customize as needed
        log.setRecipient(to);
        log.setSubject(subject);
        log.setContent(body);
        log.setSentAt(Instant.now());
        log.setTransactional(true);
        log.setTags(List.of(new Tag("testing","demo")));

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("deerajkhakre@gmail.com");
            helper.setText(body, true);
            javaMailSender.send(message);
            log.setStatus(EmailStatus.SENT);
            log.setRetryCount(0);
            emailRepository.save(log);  // 📥 Persist log entry
        } catch (Exception e) {
            log.setStatus(EmailStatus.FAILED);
            log.setRetryCount(1); // or increment dynamically
            System.err.println("Email send failed: " + e.getMessage());
            emailRepository.save(log);  // 📥 Persist log entry
        }
        return log.getStatus() == EmailStatus.SENT;
    }


}
