package com.saga.payment.domain.entity;

import com.saga.common.domain.entity.AggregateRoot;
import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.UserId;
import com.saga.payment.domain.exception.PaymentDomainException;

public class UserBalance extends AggregateRoot<UserId> {

    private Money balance;

    private UserBalance(Builder builder) {
        setId(builder.userId);
        this.balance = builder.balance;
    }

    public void reserve(Money amount) {
        if (!balance.isGreaterThanOrEqualTo(amount)) {
            throw new PaymentDomainException(
                "Insufficient balance for userId: " + getId().getValue()
                    + ". Required: " + amount + ", Available: " + balance);
        }
        this.balance = balance.subtract(amount);
    }

    public void refund(Money amount) {
        this.balance = balance.add(amount);
    }

    public Money getBalance() {
        return balance;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UserId userId;
        private Money balance;

        private Builder() {
        }

        public Builder userId(UserId val) {
            userId = val;
            return this;
        }

        public Builder balance(Money val) {
            balance = val;
            return this;
        }

        public UserBalance build() {
            return new UserBalance(this);
        }
    }
}
