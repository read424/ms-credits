package com.bootcamp.ms_credits.infrastructure.adapters.outbound.persistence.repository;

import com.bootcamp.ms_credits.domain.model.CreditType;
import com.bootcamp.ms_credits.infrastructure.adapters.outbound.persistence.document.CreditDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditMongoRepository extends ReactiveMongoRepository<CreditDocument, String> {
    Flux<CreditDocument> findByCustomerId(String customerId);
    Mono<Long> countByCustomerIdAndType(String customerId, CreditType type);
}
