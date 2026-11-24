package com.saga.order.domain.exception;

public class OrderNotFoundException extends OrderDomainException {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
