package com.dk.userservice.service;

import com.dk.userservice.models.EmailOutbox;
import com.dk.userservice.models.User;
import com.dk.userservice.repository.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailOutBoxService {

    public  final EmailOutboxRepository emailOutboxRepository;

    public  boolean createOutBox(User user, String eventType ){
      try
      {
          Map<String, Object> variables = new HashMap<>();
          variables.put("firstName", user.getFirstName());
          variables.put("baseUrl", "https://shopx.example.com");
          variables.put("supportEmail", "support@shopx.example.com");
          variables.put("sentAt", Instant.now().toString());
          // insert an outbox entry for welcome email
          EmailOutbox outbox = EmailOutbox.builder()
                  .aggregateId(user.getId())
                  .eventType(eventType)
                  .recipient(user.getEmail())
                  .subject("Welcome to ShopX!")
                  .template("WELCOME_TEMPLATE")
                  .variables(variables)
                  .published(false)
                  .retryCount(0)
                  .createdAt(Instant.now())
                  .build();

          emailOutboxRepository.save(outbox);
          return  true;
      }catch(RuntimeException e){
          log.error("insert an outbox entry for welcome email is false  with email {}",user.getEmail());
          return false;
      }
    }
}
