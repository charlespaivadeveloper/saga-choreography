package com.saga.payment.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class SagaPaymentMetrics {

    private final Counter paymentReserved;
    private final Counter paymentRejected;
    private final Counter paymentRefunded;

    public SagaPaymentMetrics(MeterRegistry registry) {
        this.paymentReserved = Counter.builder("saga.payment.reserved")
                .description("Payment successfully reserved for an order")
                .register(registry);
        this.paymentRejected = Counter.builder("saga.payment.rejected")
                .description("Payment rejected (e.g. insufficient balance)")
                .register(registry);
        this.paymentRefunded = Counter.builder("saga.payment.refunded")
                .description("Payment refunded during SAGA compensation")
                .register(registry);
    }

    public void paymentReserved() {
        paymentReserved.increment();
    }

    public void paymentRejected() {
        paymentRejected.increment();
    }

    public void paymentRefunded() {
        paymentRefunded.increment();
    }
}
