package com.saga.inventory.domain.event;

import com.saga.common.domain.event.DomainEvent;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.ProductId;

import java.time.Instant;
import java.util.List;

public class InventoryRejectedEvent implements DomainEvent<OrderId> {

    private final OrderId orderId;
    private final ProductId productId;
    private final List<String> failureMessages;
    private final Instant createdAt;

    public InventoryRejectedEvent(OrderId orderId, ProductId productId,
                                   List<String> failureMessages, Instant createdAt) {
        this.orderId = orderId;
        this.productId = productId;
        this.failureMessages = failureMessages;
        this.createdAt = createdAt;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
