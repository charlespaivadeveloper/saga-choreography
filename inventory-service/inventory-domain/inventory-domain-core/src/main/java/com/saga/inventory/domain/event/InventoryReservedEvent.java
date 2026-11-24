package com.saga.inventory.domain.event;

import com.saga.common.domain.event.DomainEvent;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.ProductId;

import java.time.Instant;

public class InventoryReservedEvent implements DomainEvent<OrderId> {

    private final OrderId orderId;
    private final ProductId productId;
    private final Instant createdAt;

    public InventoryReservedEvent(OrderId orderId, ProductId productId, Instant createdAt) {
        this.orderId = orderId;
        this.productId = productId;
        this.createdAt = createdAt;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
