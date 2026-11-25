package com.saga.inventory.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class SagaInventoryMetrics {

    private final Counter inventoryReserved;
    private final Counter inventoryRejected;
    private final Counter inventoryRestored;

    public SagaInventoryMetrics(MeterRegistry registry) {
        this.inventoryReserved = Counter.builder("saga.inventory.reserved")
                .description("Inventory successfully reserved for an order")
                .register(registry);
        this.inventoryRejected = Counter.builder("saga.inventory.rejected")
                .description("Inventory reservation rejected (e.g. out of stock)")
                .register(registry);
        this.inventoryRestored = Counter.builder("saga.inventory.restored")
                .description("Inventory restored during SAGA compensation")
                .register(registry);
    }

    public void inventoryReserved() {
        inventoryReserved.increment();
    }

    public void inventoryRejected() {
        inventoryRejected.increment();
    }

    public void inventoryRestored() {
        inventoryRestored.increment();
    }
}
