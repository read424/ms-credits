package com.bootcamp.ms_credits.infrastructure.adapters.inbound.rest.handler;

import com.bootcamp.ms_credits.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CreditNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNotFound(CreditNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CreditAlreadyExistsException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleConflict(CreditAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleBadRequest(BusinessRuleViolationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InsufficientCreditLimitException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUnprocessable(InsufficientCreditLimitException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    private Mono<ResponseEntity<Map<String, Object>>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "timestamp", Instant.now().toString()
        );
        return Mono.just(ResponseEntity.status(status).body(body));
    }
}
