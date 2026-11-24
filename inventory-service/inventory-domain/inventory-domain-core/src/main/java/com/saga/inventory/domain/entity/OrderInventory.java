package com.saga.inventory.domain.entity;

import com.saga.common.domain.entity.AggregateRoot;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.exception.InventoryDomainException;

public class OrderInventory extends AggregateRoot<ProductId> {

    private int availableQuantity;

    private OrderInventory(Builder builder) {
        setId(builder.productId);
        this.availableQuantity = builder.availableQuantity;
    }

    public void reserve() {
        if (availableQuantity <= 0) {
            throw new InventoryDomainException(
                "Insufficient inventory for productId: " + getId().getValue()
                    + ". Available: " + availableQuantity);
        }
        this.availableQuantity--;
    }

    public void restore() {
        this.availableQuantity++;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ProductId productId;
        private int availableQuantity;

        private Builder() {
        }

        public Builder productId(ProductId val) {
            productId = val;
            return this;
        }

        public Builder availableQuantity(int val) {
            availableQuantity = val;
            return this;
        }

        public OrderInventory build() {
            return new OrderInventory(this);
        }
    }
}
