package com.saga.payment.domain.event;

import com.saga.common.domain.event.DomainEvent;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.UserId;

import java.time.Instant;
import java.util.List;

public class PaymentRejectedEvent implements DomainEvent<OrderId> {

    private final OrderId orderId;
    private final UserId userId;
    private final List<String> failureMessages;
    private final Instant createdAt;

    public PaymentRejectedEvent(OrderId orderId, UserId userId, List<String> failureMessages, Instant createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.failureMessages = failureMessages;
        this.createdAt = createdAt;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public UserId getUserId() {
        return userId;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
