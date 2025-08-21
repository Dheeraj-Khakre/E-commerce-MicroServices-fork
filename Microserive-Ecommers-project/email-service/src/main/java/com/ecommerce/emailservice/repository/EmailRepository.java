package com.ecommerce.emailservice.repository;

import com.ecommerce.emailservice.models.EmailLog;
import com.ecommerce.emailservice.models.EmailStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.Instant;
import java.util.List;

public interface EmailRepository extends MongoRepository<EmailLog, String> {

    @Query("{ 'status': { $in: ?0 }, $or: [ { 'nextAttemptAt': null }, { 'nextAttemptAt': { $lte: ?1 } } ] }")
    List<EmailLog> findReadyForRetry(List<EmailStatus> statuses, Instant now, Pageable pageable);

    // optional convenience for single-status
    default List<EmailLog> findFailedReadyForRetry(Pageable pageable) {
        return findReadyForRetry(List.of(EmailStatus.FAILED), Instant.now(), pageable);
    }
}