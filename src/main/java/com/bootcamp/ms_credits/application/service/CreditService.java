package com.bootcamp.ms_credits.application.service;

import com.bootcamp.ms_credits.application.ports.input.*;
import com.bootcamp.ms_credits.application.ports.output.CreditRepositoryPort;
import com.bootcamp.ms_credits.application.ports.output.CustomerLookupPort;
import com.bootcamp.ms_credits.application.ports.output.CreditEventPublisherPort;
import com.bootcamp.ms_credits.domain.exception.*;
import com.bootcamp.ms_credits.domain.model.*;
import com.bootcamp.ms_credits.domain.model.event.CreditCreatedEvent;
import com.bootcamp.ms_credits.domain.model.event.CreditPaymentCompletedEvent;
import com.bootcamp.ms_credits.domain.model.event.CreditChargeCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementación de todos los casos de uso de crédito.
 * Contiene únicamente lógica de negocio pura (RN-01 a RN-09).
 */
@Service
@RequiredArgsConstructor
public class CreditService implements
        CreateCreditUseCase,
        FindCreditUseCase,
        UpdateCreditUseCase,
        DeleteCreditUseCase,
        ProcessPaymentUseCase,
        ProcessChargeUseCase {

    private final CreditRepositoryPort creditRepository;
    private final CustomerLookupPort customerLookup;
    private final CreditEventPublisherPort eventPublisher;

    // ------------------------------------------------------------------ CREATE
    /**
     * RN-01: PERSONAL_LOAN único por cliente PERSONAL.
     * RN-02: tipo de crédito compatible con tipo de cliente.
     * RN-03: BUSINESS_LOAN ilimitado.
     * RN-04: CREDIT_CARD única por customerId + customerType.
     * RN-05: no requiere cuenta bancaria.
     */
    @Override
    public Mono<Credit> createCredit(Credit credit) {
        return customerLookup.findCustomerType(credit.getCustomerId())
                .flatMap(customerType -> {
                    credit.setCustomerType(customerType);
                    return validateBusinessRules(credit, customerType);
                })
                .flatMap(validated -> {
                    validated.setStatus(CreditStatus.ACTIVE);
                    validated.setCreditNumber(generateCreditNumber());
                    if (validated.getType() == CreditType.CREDIT_CARD) {
                        validated.setAvailableCredit(validated.getCreditLimit());
                    } else {
                        validated.setOutstandingBalance(validated.getAmount());
                    }
                    return creditRepository.save(validated)
                            .flatMap(savedCredit -> {
                                CreditCreatedEvent event = CreditCreatedEvent.builder()
                                        .id(savedCredit.getId())
                                        .creditNumber(savedCredit.getCreditNumber())
                                        .customerId(savedCredit.getCustomerId())
                                        .type(savedCredit.getType().name())
                                        .amount(savedCredit.getAmount())
                                        .outstandingBalance(savedCredit.getOutstandingBalance())
                                        .interestRate(savedCredit.getInterestRate())
                                        .creditLimit(savedCredit.getCreditLimit())
                                        .availableCredit(savedCredit.getAvailableCredit())
                                        .customerType(savedCredit.getCustomerType().name())
                                        .status(savedCredit.getStatus().name())
                                        .createdAt(LocalDateTime.now())
                                        .build();
                                return eventPublisher.publishCreditCreated(event)
                                        .thenReturn(savedCredit);
                            });
                });
    }

    // ------------------------------------------------------------------ FIND
    @Override
    public Flux<Credit> findAll() {
        return creditRepository.findAll();
    }

    @Override
    public Mono<Credit> findById(String id) {
        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(id)));
    }

    @Override
    public Flux<Credit> findByCustomerId(String customerId) {
        return creditRepository.findByCustomerId(customerId);
    }

    // ------------------------------------------------------------------ UPDATE
    @Override
    public Mono<Credit> updateCredit(String id, Credit updated) {
        return findById(id)
                .map(existing -> {
                    existing.setInterestRate(updated.getInterestRate());
                    existing.setAmount(updated.getAmount());
                    return existing;
                })
                .flatMap(creditRepository::save);
    }

    // ------------------------------------------------------------------ DELETE
    @Override
    public Mono<Void> deleteCredit(String id) {
        return findById(id)
                .flatMap(credit -> creditRepository.deleteById(credit.getId()));
    }

    // ------------------------------------------------------------------ PAYMENT
    /**
     * RN-06: préstamos → resta de outstandingBalance; cierra si llega a 0.
     * RN-07: tarjeta   → suma a availableCredit sin superar creditLimit.
     */
    @Override
    public Mono<Credit> processPayment(String creditId, Double amount) {
        return findById(creditId)
                .map(credit -> {
                    if (credit.getType() == CreditType.CREDIT_CARD) {
                        // RN-07
                        double newAvailable = Math.min(
                                credit.getAvailableCredit() + amount,
                                credit.getCreditLimit());
                        credit.setAvailableCredit(newAvailable);
                    } else {
                        // RN-06
                        double newBalance = Math.max(credit.getOutstandingBalance() - amount, 0.0);
                        credit.setOutstandingBalance(newBalance);
                        if (newBalance == 0.0) {
                            credit.setStatus(CreditStatus.CLOSED);
                        }
                    }
                    return credit;
                })
                .flatMap(creditRepository::save)
                .flatMap(updatedCredit -> {
                    CreditPaymentCompletedEvent event = CreditPaymentCompletedEvent.builder()
                            .id(updatedCredit.getId())
                            .creditNumber(updatedCredit.getCreditNumber())
                            .customerId(updatedCredit.getCustomerId())
                            .type(updatedCredit.getType().name())
                            .amount(amount)
                            .newOutstandingBalance(updatedCredit.getOutstandingBalance())
                            .newAvailableCredit(updatedCredit.getAvailableCredit())
                            .transactionId(generateTransactionId())
                            .completedAt(LocalDateTime.now())
                            .build();
                    return eventPublisher.publishPaymentCompleted(event)
                            .thenReturn(updatedCredit);
                });
    }

    // ------------------------------------------------------------------ CHARGE
    /**
     * RN-08: solo CREDIT_CARD; rechaza si amount > availableCredit.
     */
    @Override
    public Mono<Credit> processCharge(String creditId, Double amount) {
        return findById(creditId)
                .flatMap(credit -> {
                    if (credit.getType() != CreditType.CREDIT_CARD) {
                        return Mono.error(new BusinessRuleViolationException(
                                "Charge operation is only allowed for CREDIT_CARD products"));
                    }
                    if (amount > credit.getAvailableCredit()) {
                        return Mono.error(new InsufficientCreditLimitException(
                                "Charge amount " + amount + " exceeds available credit " + credit.getAvailableCredit()));
                    }
                    credit.setAvailableCredit(credit.getAvailableCredit() - amount);
                    return creditRepository.save(credit)
                            .flatMap(updatedCredit -> {
                                CreditChargeCompletedEvent event = CreditChargeCompletedEvent.builder()
                                        .id(updatedCredit.getId())
                                        .creditNumber(updatedCredit.getCreditNumber())
                                        .customerId(updatedCredit.getCustomerId())
                                        .type(updatedCredit.getType().name())
                                        .amount(amount)
                                        .newAvailableCredit(updatedCredit.getAvailableCredit())
                                        .transactionId(generateTransactionId())
                                        .completedAt(LocalDateTime.now())
                                        .build();
                                return eventPublisher.publishChargeCompleted(event)
                                        .thenReturn(updatedCredit);
                            });
                });
    }

    // ------------------------------------------------------------------ HELPERS
    private Mono<Credit> validateBusinessRules(Credit credit, CustomerType customerType) {
        return switch (credit.getType()) {
            case PERSONAL_LOAN -> validatePersonalLoan(credit, customerType);
            case BUSINESS_LOAN -> validateBusinessLoan(credit, customerType);
            case CREDIT_CARD   -> validateCreditCard(credit);
        };
    }

    /** RN-01 + RN-02 */
    private Mono<Credit> validatePersonalLoan(Credit credit, CustomerType customerType) {
        if (customerType != CustomerType.PERSONAL) {
            return Mono.error(new BusinessRuleViolationException(
                    "PERSONAL_LOAN can only be assigned to PERSONAL customers"));
        }
        return creditRepository.countByCustomerIdAndType(credit.getCustomerId(), CreditType.PERSONAL_LOAN)
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new CreditAlreadyExistsException(
                                "Customer already has an active PERSONAL_LOAN"));
                    }
                    return Mono.just(credit);
                });
    }

    /** RN-02 + RN-03 */
    private Mono<Credit> validateBusinessLoan(Credit credit, CustomerType customerType) {
        if (customerType != CustomerType.BUSINESS) {
            return Mono.error(new BusinessRuleViolationException(
                    "BUSINESS_LOAN can only be assigned to BUSINESS customers"));
        }
        return Mono.just(credit); // ilimitado
    }

    /** RN-04 */
    private Mono<Credit> validateCreditCard(Credit credit) {
        return creditRepository.countByCustomerIdAndType(credit.getCustomerId(), CreditType.CREDIT_CARD)
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new CreditAlreadyExistsException(
                                "Customer already has an active CREDIT_CARD"));
                    }
                    return Mono.just(credit);
                });
    }

    private String generateCreditNumber() {
        return "CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateTransactionId() {
        return "tx-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
