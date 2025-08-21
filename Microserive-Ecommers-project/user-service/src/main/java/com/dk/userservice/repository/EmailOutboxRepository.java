package com.dk.userservice.repository;


import com.dk.userservice.models.EmailOutbox;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EmailOutboxRepository extends MongoRepository<EmailOutbox, String> {
    List<EmailOutbox> findTop50ByPublishedFalseOrderByCreatedAtAsc();
}
