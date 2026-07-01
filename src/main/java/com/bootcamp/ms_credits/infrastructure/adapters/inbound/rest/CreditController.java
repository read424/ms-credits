package com.bootcamp.ms_credits.infrastructure.adapters.inbound.rest;

import com.bootcamp.ms_credits.application.ports.input.*;
import com.bootcamp.ms_credits.domain.model.CreditType;
import com.bootcamp.ms_credits.infrastructure.adapters.inbound.rest.dto.*;
import com.bootcamp.ms_credits.infrastructure.adapters.inbound.rest.mapper.CreditDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreateCreditUseCase createCreditUseCase;
    private final FindCreditUseCase findCreditUseCase;
    private final UpdateCreditUseCase updateCreditUseCase;
    private final DeleteCreditUseCase deleteCreditUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final ProcessChargeUseCase processChargeUseCase;
    private final CreditDtoMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreditResponse> createCredit(@Valid @RequestBody CreditRequest request) {
        return createCreditUseCase.createCredit(mapper.toDomain(request))
                .map(mapper::toResponse);
    }

    @GetMapping
    public Flux<CreditResponse> findAll() {
        return findCreditUseCase.findAll().map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<CreditResponse> findById(@PathVariable String id) {
        return findCreditUseCase.findById(id).map(mapper::toResponse);
    }

    @GetMapping("/customer/{customerId}")
    public Flux<CreditResponse> findByCustomer(@PathVariable String customerId) {
        return findCreditUseCase.findByCustomerId(customerId).map(mapper::toResponse);
    }

    @PutMapping("/{id}")
    public Mono<CreditResponse> update(@PathVariable String id,
                                       @Valid @RequestBody CreditRequest request) {
        return updateCreditUseCase.updateCredit(id, mapper.toDomain(request))
                .map(mapper::toResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return deleteCreditUseCase.deleteCredit(id);
    }

    @PostMapping("/{id}/payments")
    public Mono<CreditResponse> processPayment(@PathVariable String id,
                                               @Valid @RequestBody PaymentRequest request) {
        return processPaymentUseCase.processPayment(id, request.getAmount())
                .map(mapper::toResponse);
    }

    @PostMapping("/{id}/charges")
    public Mono<CreditResponse> processCharge(@PathVariable String id,
                                              @Valid @RequestBody ChargeRequest request) {
        return processChargeUseCase.processCharge(id, request.getAmount())
                .map(mapper::toResponse);
    }

    /** RN-09: respuesta varía según tipo de producto */
    @GetMapping("/{id}/balance")
    public Mono<BalanceResponse> getBalance(@PathVariable String id) {
        return findCreditUseCase.findById(id)
                .map(credit -> {
                    if (credit.getType() == CreditType.CREDIT_CARD) {
                        return BalanceResponse.builder()
                                .creditId(credit.getId())
                                .type(credit.getType())
                                .creditLimit(credit.getCreditLimit())
                                .availableCredit(credit.getAvailableCredit())
                                .build();
                    } else {
                        return BalanceResponse.builder()
                                .creditId(credit.getId())
                                .type(credit.getType())
                                .outstandingBalance(credit.getOutstandingBalance())
                                .build();
                    }
                });
    }
}
