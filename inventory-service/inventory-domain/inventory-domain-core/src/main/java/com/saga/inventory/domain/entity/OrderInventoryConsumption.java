package com.saga.inventory.domain.entity;

import com.saga.common.domain.entity.BaseEntity;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.ProductId;

import java.util.UUID;

public class OrderInventoryConsumption extends BaseEntity<UUID> {

    private final ProductId productId;
    private final OrderId orderId;
    private final int quantityConsumed;

    private OrderInventoryConsumption(Builder builder) {
        setId(builder.consumptionId != null ? builder.consumptionId : UUID.randomUUID());
        this.productId = builder.productId;
        this.orderId = builder.orderId;
        this.quantityConsumed = builder.quantityConsumed;
    }

    public ProductId getProductId() {
        return productId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public int getQuantityConsumed() {
        return quantityConsumed;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID consumptionId;
        private ProductId productId;
        private OrderId orderId;
        private int quantityConsumed = 1;

        private Builder() {
        }

        public Builder consumptionId(UUID val) {
            consumptionId = val;
            return this;
        }

        public Builder productId(ProductId val) {
            productId = val;
            return this;
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder quantityConsumed(int val) {
            quantityConsumed = val;
            return this;
        }

        public OrderInventoryConsumption build() {
            return new OrderInventoryConsumption(this);
        }
    }
}
