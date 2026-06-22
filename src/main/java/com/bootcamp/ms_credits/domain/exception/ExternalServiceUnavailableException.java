package com.bootcamp.ms_credits.domain.exception;

public class ExternalServiceUnavailableException extends RuntimeException {

    private final String serviceName;
    private final String message;

    public ExternalServiceUnavailableException(String serviceName, String message) {
        super(String.format("External service '%s' is unavailable: %s", serviceName, message));
        this.serviceName = serviceName;
        this.message = message;
    }

    public ExternalServiceUnavailableException(String serviceName, String message, Throwable cause) {
        super(String.format("External service '%s' is unavailable: %s", serviceName, message), cause);
        this.serviceName = serviceName;
        this.message = message;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDetailMessage() {
        return message;
    }
}
