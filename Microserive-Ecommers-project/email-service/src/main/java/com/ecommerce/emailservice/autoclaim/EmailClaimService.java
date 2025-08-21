package com.ecommerce.emailservice.autoclaim;

import com.ecommerce.emailservice.models.EmailLog;
import com.ecommerce.emailservice.models.EmailStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailClaimService {

    private final MongoTemplate mongoTemplate;

    /**
     * Atomically claim one email document ready for retry and set its status to PROCESSING.
     * Returns the claimed EmailLog or null if none available.
     */
    public EmailLog claimOneForProcessing() {
        Query q = new Query();
        q.addCriteria(Criteria.where("status").is(EmailStatus.FAILED));
        q.addCriteria(new Criteria().orOperator(
                Criteria.where("nextAttemptAt").is(null),
                Criteria.where("nextAttemptAt").lte(Instant.now())
        ));
        q.with(Sort.by(Sort.Direction.ASC, "createdAt"));

        Update update = new Update()
                .set("status", EmailStatus.PROCESSING)
                .set("processingAt", Instant.now());

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        return mongoTemplate.findAndModify(q, update, options, EmailLog.class);
    }
}
