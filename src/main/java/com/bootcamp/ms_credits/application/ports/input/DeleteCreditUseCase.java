package com.bootcamp.ms_credits.application.ports.input;

import reactor.core.publisher.Mono;

public interface DeleteCreditUseCase {
    Mono<Void> deleteCredit(String id);
}
