package com.bootcamp.ms_credits.application.ports.input;

import com.bootcamp.ms_credits.domain.model.Credit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FindCreditUseCase {
    Flux<Credit> findAll();
    Mono<Credit> findById(String id);
    Flux<Credit> findByCustomerId(String customerId);
}
