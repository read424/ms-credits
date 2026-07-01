package com.bootcamp.ms_credits.application.ports.output;

import com.bootcamp.ms_credits.domain.model.event.CreditCreatedEvent;
import com.bootcamp.ms_credits.domain.model.event.CreditPaymentCompletedEvent;
import com.bootcamp.ms_credits.domain.model.event.CreditChargeCompletedEvent;
import reactor.core.publisher.Mono;

public interface CreditEventPublisherPort {
    Mono<Void> publishCreditCreated(CreditCreatedEvent event);
    Mono<Void> publishPaymentCompleted(CreditPaymentCompletedEvent event);
    Mono<Void> publishChargeCompleted(CreditChargeCompletedEvent event);
}
