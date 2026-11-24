package com.saga.order.domain.service;

import com.saga.common.domain.valueobject.InventoryStatus;
import com.saga.common.domain.valueobject.PaymentStatus;
import com.saga.order.domain.entity.Order;
import com.saga.order.domain.event.OrderCancelledEvent;
import com.saga.order.domain.event.OrderCompletedEvent;
import com.saga.order.domain.event.OrderCreatedEvent;
import com.saga.order.domain.exception.OrderDomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class OrderDomainServiceImpl implements OrderDomainService {

    private static final Logger log = LoggerFactory.getLogger(OrderDomainServiceImpl.class);

    @Override
    public OrderCreatedEvent validateAndInitiateOrder(Order order) {
        validateOrder(order);
        order.initializeOrder();
        log.info("Order with id {} is initiated", order.getId().getValue());
        return new OrderCreatedEvent(order, Instant.now());
    }

    @Override
    public void updateOrderPaymentStatus(Order order, PaymentStatus paymentStatus, List<String> failureMessages) {
        order.processPaymentResult(paymentStatus, failureMessages);
        log.info("Order with id {} payment status updated to {}", order.getId().getValue(), paymentStatus);
    }

    @Override
    public void updateOrderInventoryStatus(Order order, InventoryStatus inventoryStatus, List<String> failureMessages) {
        order.processInventoryResult(inventoryStatus, failureMessages);
        log.info("Order with id {} inventory status updated to {}", order.getId().getValue(), inventoryStatus);
    }

    @Override
    public Optional<OrderCompletedEvent> completeOrderSaga(Order order) {
        if (!order.isReadyToComplete()) {
            return Optional.empty();
        }
        order.complete();
        log.info("Order with id {} is completed", order.getId().getValue());
        return Optional.of(new OrderCompletedEvent(order, Instant.now()));
    }

    @Override
    public Optional<OrderCancelledEvent> cancelOrderSaga(Order order) {
        if (!order.isReadyToCancel()) {
            return Optional.empty();
        }
        order.cancel();
        log.info("Order with id {} is cancelled with messages: {}", order.getId().getValue(), order.getFailureMessages());
        return Optional.of(new OrderCancelledEvent(order, Instant.now()));
    }

    private void validateOrder(Order order) {
        if (order.getPrice() == null || !order.getPrice().isGreaterThanZero()) {
            throw new OrderDomainException("Order price must be greater than zero");
        }
        if (order.getUserId() == null) {
            throw new OrderDomainException("Order userId cannot be null");
        }
        if (order.getProductId() == null) {
            throw new OrderDomainException("Order productId cannot be null");
        }
    }
}
