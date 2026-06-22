package com.bootcamp.ms_credits.domain.exception;

public class CreditAlreadyExistsException extends RuntimeException {
    public CreditAlreadyExistsException(String message) {
        super(message);
    }
}
