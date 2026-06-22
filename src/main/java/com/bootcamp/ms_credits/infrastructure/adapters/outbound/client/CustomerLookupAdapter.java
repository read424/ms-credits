package com.bootcamp.ms_credits.infrastructure.adapters.outbound.client;

import com.bootcamp.ms_credits.application.ports.out.CustomerLookupPort;
import com.bootcamp.ms_credits.domain.exception.ExternalServiceUnavailableException;
import com.bootcamp.ms_credits.domain.model.CustomerType;
import com.bootcamp.ms_credits.infrastructure.adapters.outbound.client.dto.CustomerClientResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerLookupAdapter implements CustomerLookupPort {

    private final WebClient webClient;
    private final CircuitBreaker customerServiceCircuitBreaker;

    @Value("${customer.service.url:http://CUSTOMER-SERVICE}")
    private String customerServiceUrl;

    @Override
    public Mono<CustomerType> findCustomerType(String customerId) {
        return Mono.defer(() ->
            customerServiceCircuitBreaker.executeSupplier(() ->
                performCustomerLookup(customerId)
            )
        )
        .onErrorResume(this::handleError);
    }

    private Mono<CustomerType> performCustomerLookup(String customerId) {
        log.debug("Fetching customer type for customer: {}", customerId);
        return webClient.get()
                .uri(customerServiceUrl + "/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerClientResponse.class)
                .map(response -> {
                    log.debug("Customer found: {} with type: {}", customerId, response.getCustomerType());
                    return response.getCustomerType();
                })
                .doOnError(error -> log.error("Error fetching customer type for customer {}: {}", customerId, error.getMessage()));
    }

    private Mono<CustomerType> handleError(Throwable throwable) {
        if (throwable instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            log.warn("Circuit breaker is open for customer service lookup. Returning default fallback.");
            return handleFallback(throwable);
        }

        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            log.error("Customer service returned error status {}: {}", ex.getStatusCode(), ex.getStatusText());
            return Mono.error(new ExternalServiceUnavailableException(
                    "CUSTOMER-SERVICE",
                    String.format("HTTP %s: %s", ex.getStatusCode(), ex.getStatusText()),
                    ex
            ));
        }

        log.error("Unexpected error in customer service lookup", throwable);
        return Mono.error(new ExternalServiceUnavailableException(
                "CUSTOMER-SERVICE",
                throwable.getMessage(),
                throwable
        ));
    }

    private Mono<CustomerType> handleFallback(Throwable throwable) {
        log.info("Applying fallback for customer service. Using default PERSONAL customer type.");
        return Mono.just(CustomerType.PERSONAL);
    }
}
