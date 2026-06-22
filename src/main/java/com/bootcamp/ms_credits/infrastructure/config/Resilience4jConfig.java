package com.bootcamp.ms_credits.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@Configuration
public class Resilience4jConfig {

    private static final Logger logger = LoggerFactory.getLogger(Resilience4jConfig.class);

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    public CircuitBreaker customerServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(60)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .recordExceptions(Exception.class)
                .ignoreExceptions(com.bootcamp.ms_credits.domain.exception.BusinessRuleViolationException.class)
                .build();

        if (registry.getConfiguration("customerServiceConfig").isEmpty()) {
            registry.addConfiguration("customerServiceConfig", config);
        }

        CircuitBreaker circuitBreaker = registry.circuitBreaker("customerService", "customerServiceConfig");
        logger.info("Circuit Breaker 'customerService' registered successfully");

        return circuitBreaker;
    }
}
