package com.saga.order.domain.event;

import com.saga.common.domain.event.DomainEvent;
import com.saga.order.domain.entity.Order;

import java.time.Instant;

public class OrderCompletedEvent implements DomainEvent<Order> {

    private final Order order;
    private final Instant createdAt;

    public OrderCompletedEvent(Order order, Instant createdAt) {
        this.order = order;
        this.createdAt = createdAt;
    }

    public Order getOrder() {
        return order;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
