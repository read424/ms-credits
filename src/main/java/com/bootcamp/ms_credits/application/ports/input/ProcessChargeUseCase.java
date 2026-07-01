package com.bootcamp.ms_credits.application.ports.input;

import com.bootcamp.ms_credits.domain.model.Credit;
import reactor.core.publisher.Mono;

public interface ProcessChargeUseCase {
    Mono<Credit> processCharge(String creditId, Double amount);
}
