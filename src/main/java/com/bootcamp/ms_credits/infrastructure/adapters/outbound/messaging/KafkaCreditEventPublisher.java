package com.bootcamp.ms_credits.infrastructure.adapters.outbound.messaging;

import com.bootcamp.ms_credits.application.ports.output.CreditEventPublisherPort;
import com.bootcamp.ms_credits.domain.model.event.CreditCreatedEvent;
import com.bootcamp.ms_credits.domain.model.event.CreditPaymentCompletedEvent;
import com.bootcamp.ms_credits.domain.model.event.CreditChargeCompletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaCreditEventPublisher implements CreditEventPublisherPort {

    private final KafkaSender<String, Object> kafkaSender;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.credit-created}")
    private String creditCreatedTopic;

    @Value("${kafka.topics.credit-payment-completed}")
    private String paymentCompletedTopic;

    @Value("${kafka.topics.credit-charge-completed}")
    private String chargeCompletedTopic;

    @Override
    public Mono<Void> publishCreditCreated(CreditCreatedEvent event) {
        log.info("Publishing CreditCreatedEvent for creditId: {}", event.getId());
        return sendMessage(creditCreatedTopic, event.getId(), event)
            .doOnSuccess(result -> log.info("CreditCreatedEvent published successfully"))
            .doOnError(error -> log.error("Error publishing CreditCreatedEvent", error))
            .then();
    }

    @Override
    public Mono<Void> publishPaymentCompleted(CreditPaymentCompletedEvent event) {
        log.info("Publishing CreditPaymentCompletedEvent for creditId: {}", event.getId());
        return sendMessage(paymentCompletedTopic, event.getId(), event)
            .doOnSuccess(result -> log.info("CreditPaymentCompletedEvent published successfully"))
            .doOnError(error -> log.error("Error publishing CreditPaymentCompletedEvent", error))
            .then();
    }

    @Override
    public Mono<Void> publishChargeCompleted(CreditChargeCompletedEvent event) {
        log.info("Publishing CreditChargeCompletedEvent for creditId: {}", event.getId());
        return sendMessage(chargeCompletedTopic, event.getId(), event)
            .doOnSuccess(result -> log.info("CreditChargeCompletedEvent published successfully"))
            .doOnError(error -> log.error("Error publishing CreditChargeCompletedEvent", error))
            .then();
    }

    private Mono<Void> sendMessage(String topic, String key, Object event) {
        return Mono.defer(() -> {
            SenderRecord<String, Object, Void> record = SenderRecord.create(topic, null, null, key, event, null);
            return kafkaSender.send(Mono.just(record)).then();
        });
    }
}
