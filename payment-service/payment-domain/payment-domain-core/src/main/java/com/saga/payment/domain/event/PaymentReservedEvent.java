package com.saga.payment.domain.event;

import com.saga.common.domain.event.DomainEvent;
import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.UserId;

import java.time.Instant;

public class PaymentReservedEvent implements DomainEvent<OrderId> {

    private final OrderId orderId;
    private final UserId userId;
    private final Money amount;
    private final Instant createdAt;

    public PaymentReservedEvent(OrderId orderId, UserId userId, Money amount, Instant createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public UserId getUserId() {
        return userId;
    }

    public Money getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
