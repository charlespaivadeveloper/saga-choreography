package com.saga.order.domain.entity;

import com.saga.common.domain.entity.AggregateRoot;
import com.saga.common.domain.valueobject.InventoryStatus;
import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.OrderStatus;
import com.saga.common.domain.valueobject.PaymentStatus;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.common.domain.valueobject.UserId;
import com.saga.order.domain.exception.OrderDomainException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<OrderId> {

    private final UserId userId;
    private final ProductId productId;
    private final Money price;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private InventoryStatus inventoryStatus;
    private List<String> failureMessages;

    private Order(Builder builder) {
        setId(builder.orderId);
        this.userId = builder.userId;
        this.productId = builder.productId;
        this.price = builder.price;
        this.orderStatus = builder.orderStatus;
        this.paymentStatus = builder.paymentStatus;
        this.inventoryStatus = builder.inventoryStatus;
        this.failureMessages = builder.failureMessages != null ? builder.failureMessages : new ArrayList<>();
    }

    public void initializeOrder() {
        setId(OrderId.of(UUID.randomUUID()));
        this.orderStatus = OrderStatus.ORDER_CREATED;
        this.failureMessages = new ArrayList<>();
    }

    public void processPaymentResult(PaymentStatus status, List<String> messages) {
        this.paymentStatus = status;
        if (messages != null) {
            this.failureMessages.addAll(messages);
        }
    }

    public void processInventoryResult(InventoryStatus status, List<String> messages) {
        this.inventoryStatus = status;
        if (messages != null) {
            this.failureMessages.addAll(messages);
        }
    }

    public boolean isReadyToComplete() {
        return PaymentStatus.RESERVED.equals(paymentStatus)
            && InventoryStatus.RESERVED.equals(inventoryStatus);
    }

    public boolean isReadyToCancel() {
        return PaymentStatus.REJECTED.equals(paymentStatus)
            || InventoryStatus.REJECTED.equals(inventoryStatus);
    }

    public void complete() {
        validateOrderStatus(OrderStatus.ORDER_CREATED, "complete");
        this.orderStatus = OrderStatus.ORDER_COMPLETED;
    }

    public void cancel() {
        validateOrderStatus(OrderStatus.ORDER_CREATED, "cancel");
        this.orderStatus = OrderStatus.ORDER_CANCELLED;
    }

    private void validateOrderStatus(OrderStatus expectedStatus, String operation) {
        if (!expectedStatus.equals(orderStatus)) {
            throw new OrderDomainException(
                "Cannot " + operation + " order in status: " + orderStatus);
        }
    }

    public UserId getUserId() {
        return userId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Money getPrice() {
        return price;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public InventoryStatus getInventoryStatus() {
        return inventoryStatus;
    }

    public List<String> getFailureMessages() {
        return Collections.unmodifiableList(failureMessages);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private OrderId orderId;
        private UserId userId;
        private ProductId productId;
        private Money price;
        private OrderStatus orderStatus;
        private PaymentStatus paymentStatus;
        private InventoryStatus inventoryStatus;
        private List<String> failureMessages;

        private Builder() {
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder userId(UserId val) {
            userId = val;
            return this;
        }

        public Builder productId(ProductId val) {
            productId = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder paymentStatus(PaymentStatus val) {
            paymentStatus = val;
            return this;
        }

        public Builder inventoryStatus(InventoryStatus val) {
            inventoryStatus = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
