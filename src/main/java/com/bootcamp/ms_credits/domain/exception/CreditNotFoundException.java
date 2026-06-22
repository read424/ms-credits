package com.bootcamp.ms_credits.domain.exception;

public class CreditNotFoundException extends RuntimeException {
    public CreditNotFoundException(String id) {
        super("Credit not found with id: " + id);
    }
}
