package com.bootcamp.ms_credits.application.ports.in;

import com.bootcamp.ms_credits.domain.model.Credit;
import reactor.core.publisher.Mono;

public interface UpdateCreditUseCase {
    Mono<Credit> updateCredit(String id, Credit credit);
}
