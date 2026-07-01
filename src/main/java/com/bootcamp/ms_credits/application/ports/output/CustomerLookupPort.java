package com.bootcamp.ms_credits.application.ports.output;

import com.bootcamp.ms_credits.domain.model.CustomerType;
import reactor.core.publisher.Mono;

public interface CustomerLookupPort {
    Mono<CustomerType> findCustomerType(String customerId);
}
