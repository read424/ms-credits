package com.bootcamp.ms_credits.application.ports.out;

import com.bootcamp.ms_credits.domain.model.Credit;
import com.bootcamp.ms_credits.domain.model.CreditType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditRepositoryPort {
    Mono<Credit> save(Credit credit);
    Mono<Credit> findById(String id);
    Flux<Credit> findAll();
    Flux<Credit> findByCustomerId(String customerId);
    Mono<Long> countByCustomerIdAndType(String customerId, CreditType type);
    Mono<Void> deleteById(String id);
}
