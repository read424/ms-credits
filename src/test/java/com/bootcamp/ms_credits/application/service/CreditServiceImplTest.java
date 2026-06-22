package com.bootcamp.ms_credits.application.service;

import com.bootcamp.ms_credits.application.ports.out.CreditRepositoryPort;
import com.bootcamp.ms_credits.application.ports.out.CustomerLookupPort;
import com.bootcamp.ms_credits.domain.exception.*;
import com.bootcamp.ms_credits.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditServiceImplTest {

    @Mock
    private CreditRepositoryPort creditRepository;

    @Mock
    private CustomerLookupPort customerLookup;

    @InjectMocks
    private CreditServiceImpl service;

    // ─────────────────────────────────────────────────────────── CREATE CREDIT

    @Nested
    @DisplayName("createCredit")
    class CreateCreditTests {

        @Test
        @DisplayName("RN-01: PERSONAL_LOAN — ok cuando no existe uno previo")
        void createPersonalLoan_success() {
            Credit req = creditWithType(CreditType.PERSONAL_LOAN);
            when(customerLookup.findCustomerType("cust-1")).thenReturn(Mono.just(CustomerType.PERSONAL));
            when(creditRepository.countByCustomerIdAndType("cust-1", CreditType.PERSONAL_LOAN)).thenReturn(Mono.just(0L));
            when(creditRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.createCredit(req))
                    .assertNext(c -> {
                        assertThat(c.getStatus()).isEqualTo(CreditStatus.ACTIVE);
                        assertThat(c.getOutstandingBalance()).isEqualTo(req.getAmount());
                        assertThat(c.getCustomerType()).isEqualTo(CustomerType.PERSONAL);
                        assertThat(c.getCreditNumber()).startsWith("CR-");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("RN-01: PERSONAL_LOAN — rechaza si ya tiene uno activo")
        void createPersonalLoan_alreadyExists() {
            Credit req = creditWithType(CreditType.PERSONAL_LOAN);
            when(customerLookup.findCustomerType("cust-1")).thenReturn(Mono.just(CustomerType.PERSONAL));
            when(creditRepository.countByCustomerIdAndType("cust-1", CreditType.PERSONAL_LOAN)).thenReturn(Mono.just(1L));

            StepVerifier.create(service.createCredit(req))
                    .expectError(CreditAlreadyExistsException.class)
                    .verify();
        }

        @Test
        @DisplayName("RN-02: PERSONAL_LOAN con cliente BUSINESS → 400")
        void createPersonalLoan_wrongCustomerType() {
            Credit req = creditWithType(CreditType.PERSONAL_LOAN);
            when(customerLookup.findCustomerType("cust-1")).thenReturn(Mono.just(CustomerType.BUSINESS));

            StepVerifier.create(service.createCredit(req))
                    .expectError(BusinessRuleViolationException.class)
                    .verify();
        }

        @Test
        @DisplayName("RN-02: BUSINESS_LOAN con cliente PERSONAL → 400")
        void createBusinessLoan_wrongCustomerType() {
            Credit req = creditWithType(CreditType.BUSINESS_LOAN);
            when(customerLookup.findCustomerType("cust-1")).thenReturn(Mono.just(CustomerType.PERSONAL));

            StepVerifier.create(service.createCredit(req))
                    .expectError(BusinessRuleViolationException.class)
                    .verify();
        }

        @Test
        @DisplayName("RN-03: múltiples BUSINESS_LOAN para el mismo cliente → ok")
        void createBusinessLoan_unlimited() {
            Credit req = creditWithType(CreditType.BUSINESS_LOAN);
            when(customerLookup.findCustomerType("cust-1")).thenReturn(Mono.just(CustomerType.BUSINESS));
            when(creditRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.createCredit(req))
                    .assertNext(c -> assertThat(c.getStatus()).isEqualTo(CreditStatus.ACTIVE))
                    .verifyComplete();
        }

        @Test
        @DisplayName("RN-04: CREDIT_CARD — ok cuando no existe una previa")
        void createCreditCard_success() {
            Credit req = creditCardRequest();
            when(customerLookup.findCustomerType("cust-1")).thenReturn(Mono.just(CustomerType.PERSONAL));
            when(creditRepository.countByCustomerIdAndType("cust-1", CreditType.CREDIT_CARD)).thenReturn(Mono.just(0L));
            when(creditRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.createCredit(req))
                    .assertNext(c -> {
                        assertThat(c.getAvailableCredit()).isEqualTo(req.getCreditLimit());
                        assertThat(c.getStatus()).isEqualTo(CreditStatus.ACTIVE);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("RN-04: CREDIT_CARD — rechaza si ya tiene una")
        void createCreditCard_alreadyExists() {
            Credit req = creditCardRequest();
            when(customerLookup.findCustomerType("cust-1")).thenReturn(Mono.just(CustomerType.PERSONAL));
            when(creditRepository.countByCustomerIdAndType("cust-1", CreditType.CREDIT_CARD)).thenReturn(Mono.just(1L));

            StepVerifier.create(service.createCredit(req))
                    .expectError(CreditAlreadyExistsException.class)
                    .verify();
        }
    }

    // ─────────────────────────────────────────────────────────── FIND CREDIT

    @Nested
    @DisplayName("findById")
    class FindCreditTests {

        @Test
        @DisplayName("devuelve crédito cuando existe")
        void findById_found() {
            Credit stored = activeLoan("id-1");
            when(creditRepository.findById("id-1")).thenReturn(Mono.just(stored));

            StepVerifier.create(service.findById("id-1"))
                    .expectNext(stored)
                    .verifyComplete();
        }

        @Test
        @DisplayName("lanza CreditNotFoundException cuando no existe")
        void findById_notFound() {
            when(creditRepository.findById("id-x")).thenReturn(Mono.empty());

            StepVerifier.create(service.findById("id-x"))
                    .expectError(CreditNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("findAll devuelve todos los créditos")
        void findAll_returnsAll() {
            when(creditRepository.findAll()).thenReturn(Flux.just(activeLoan("1"), activeLoan("2")));

            StepVerifier.create(service.findAll())
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("findByCustomerId devuelve créditos del cliente")
        void findByCustomer() {
            when(creditRepository.findByCustomerId("cust-1")).thenReturn(Flux.just(activeLoan("1")));

            StepVerifier.create(service.findByCustomerId("cust-1"))
                    .expectNextCount(1)
                    .verifyComplete();
        }
    }

    // ─────────────────────────────────────────────────────────── PAYMENT

    @Nested
    @DisplayName("processPayment")
    class ProcessPaymentTests {

        @Test
        @DisplayName("RN-06: pago en PERSONAL_LOAN reduce outstandingBalance")
        void payment_personalLoan_reducesBalance() {
            Credit loan = activeLoan("id-1");
            loan.setOutstandingBalance(1000.0);
            when(creditRepository.findById("id-1")).thenReturn(Mono.just(loan));
            when(creditRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.processPayment("id-1", 300.0))
                    .assertNext(c -> assertThat(c.getOutstandingBalance()).isEqualTo(700.0))
                    .verifyComplete();
        }

        @Test
        @DisplayName("RN-06: pago que deja saldo en 0 cierra el crédito")
        void payment_personalLoan_closesWhenBalanceZero() {
            Credit loan = activeLoan("id-1");
            loan.setOutstandingBalance(300.0);
            when(creditRepository.findById("id-1")).thenReturn(Mono.just(loan));
            when(creditRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.processPayment("id-1", 300.0))
                    .assertNext(c -> {
                        assertThat(c.getOutstandingBalance()).isEqualTo(0.0);
                        assertThat(c.getStatus()).isEqualTo(CreditStatus.CLOSED);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("RN-06: saldo nunca es negativo (Math.max)")
        void payment_personalLoan_balanceNeverNegative() {
            Credit loan = activeLoan("id-1");
            loan.setOutstandingBalance(100.0);
            when(creditRepository.findById("id-1")).thenReturn(Mono.just(loan));
            when(creditRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.processPayment("id-1", 500.0))
                    .assertNext(c -> assertThat(c.getOutstandingBalance()).isEqualTo(0.0))
                    .verifyComplete();
        }

        @Test
        @DisplayName("RN-07: pago en CREDIT_CARD incrementa availableCredit")
        void payment_creditCard_increasesAvailable() {
            Credit card = activeCreditCard("id-2");
            card.setAvailableCredit(200.0);
            card.setCreditLimit(1000.0);
            when(creditRepository.findById("id-2")).thenReturn(Mono.just(card));
            when(creditRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.processPayment("id-2", 300.0))
                    .assertNext(c -> assertThat(c.getAvailableCredit()).isEqualTo(500.0))
                    .verifyComplete();
        }

        @Test
        @DisplayName("RN-07: availableCredit no supera creditLimit")
        void payment_creditCard_doesNotExceedLimit() {
            Credit card = activeCreditCard("id-2");
            card.setAvailableCredit(900.0);
            card.setCreditLimit(1000.0);
            when(creditRepository.findById("id-2")).thenReturn(Mono.just(card));
            when(creditRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.processPayment("id-2", 500.0))
                    .assertNext(c -> assertThat(c.getAvailableCredit()).isEqualTo(1000.0))
                    .verifyComplete();
        }
    }

    // ─────────────────────────────────────────────────────────── CHARGE

    @Nested
    @DisplayName("processCharge")
    class ProcessChargeTests {

        @Test
        @DisplayName("RN-08: consumo válido reduce availableCredit")
        void charge_valid() {
            Credit card = activeCreditCard("id-2");
            card.setAvailableCredit(500.0);
            when(creditRepository.findById("id-2")).thenReturn(Mono.just(card));
            when(creditRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(service.processCharge("id-2", 200.0))
                    .assertNext(c -> assertThat(c.getAvailableCredit()).isEqualTo(300.0))
                    .verifyComplete();
        }

        @Test
        @DisplayName("RN-08: consumo mayor que availableCredit → InsufficientCreditLimitException")
        void charge_exceedsAvailable() {
            Credit card = activeCreditCard("id-2");
            card.setAvailableCredit(100.0);
            when(creditRepository.findById("id-2")).thenReturn(Mono.just(card));

            StepVerifier.create(service.processCharge("id-2", 500.0))
                    .expectError(InsufficientCreditLimitException.class)
                    .verify();
        }

        @Test
        @DisplayName("RN-08: consumo en préstamo → BusinessRuleViolationException")
        void charge_onLoan_rejected() {
            Credit loan = activeLoan("id-1");
            when(creditRepository.findById("id-1")).thenReturn(Mono.just(loan));

            StepVerifier.create(service.processCharge("id-1", 100.0))
                    .expectError(BusinessRuleViolationException.class)
                    .verify();
        }
    }

    // ─────────────────────────────────────────────────────────── HELPERS

    private Credit creditWithType(CreditType type) {
        return Credit.builder()
                .customerId("cust-1")
                .type(type)
                .amount(5000.0)
                .interestRate(0.15)
                .build();
    }

    private Credit creditCardRequest() {
        return Credit.builder()
                .customerId("cust-1")
                .type(CreditType.CREDIT_CARD)
                .creditLimit(2000.0)
                .interestRate(0.20)
                .build();
    }

    private Credit activeLoan(String id) {
        return Credit.builder()
                .id(id)
                .type(CreditType.PERSONAL_LOAN)
                .customerId("cust-1")
                .customerType(CustomerType.PERSONAL)
                .amount(5000.0)
                .outstandingBalance(5000.0)
                .status(CreditStatus.ACTIVE)
                .build();
    }

    private Credit activeCreditCard(String id) {
        return Credit.builder()
                .id(id)
                .type(CreditType.CREDIT_CARD)
                .customerId("cust-1")
                .customerType(CustomerType.PERSONAL)
                .creditLimit(1000.0)
                .availableCredit(1000.0)
                .status(CreditStatus.ACTIVE)
                .build();
    }
}
