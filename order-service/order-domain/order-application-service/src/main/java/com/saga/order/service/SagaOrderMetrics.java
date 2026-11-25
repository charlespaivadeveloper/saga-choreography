package com.saga.order.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class SagaOrderMetrics {

    private final Counter ordersCreated;
    private final Counter sagaCompleted;
    private final Counter sagaCancelled;

    public SagaOrderMetrics(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("saga.order.created")
                .description("Number of orders initiated via SAGA")
                .register(registry);
        this.sagaCompleted = Counter.builder("saga.completed")
                .description("Number of SAGA transactions completed successfully")
                .register(registry);
        this.sagaCancelled = Counter.builder("saga.cancelled")
                .description("Number of SAGA transactions cancelled/compensated")
                .register(registry);
    }

    public void orderCreated() {
        ordersCreated.increment();
    }

    public void sagaCompleted() {
        sagaCompleted.increment();
    }

    public void sagaCancelled() {
        sagaCancelled.increment();
    }
}
