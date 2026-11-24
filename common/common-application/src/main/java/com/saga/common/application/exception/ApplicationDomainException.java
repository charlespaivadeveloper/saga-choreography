package com.saga.common.application.exception;

public class ApplicationDomainException extends RuntimeException {

    public ApplicationDomainException(String message) {
        super(message);
    }

    public ApplicationDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
