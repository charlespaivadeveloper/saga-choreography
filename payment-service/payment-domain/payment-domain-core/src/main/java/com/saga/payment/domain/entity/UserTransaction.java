package com.saga.payment.domain.entity;

import com.saga.common.domain.entity.BaseEntity;
import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.UserId;

import java.util.UUID;

public class UserTransaction extends BaseEntity<UUID> {

    private final UserId userId;
    private final OrderId orderId;
    private final Money amount;

    private UserTransaction(Builder builder) {
        setId(builder.transactionId != null ? builder.transactionId : UUID.randomUUID());
        this.userId = builder.userId;
        this.orderId = builder.orderId;
        this.amount = builder.amount;
    }

    public UserId getUserId() {
        return userId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Money getAmount() {
        return amount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID transactionId;
        private UserId userId;
        private OrderId orderId;
        private Money amount;

        private Builder() {
        }

        public Builder transactionId(UUID val) {
            transactionId = val;
            return this;
        }

        public Builder userId(UserId val) {
            userId = val;
            return this;
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder amount(Money val) {
            amount = val;
            return this;
        }

        public UserTransaction build() {
            return new UserTransaction(this);
        }
    }
}
