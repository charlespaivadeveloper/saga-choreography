package com.saga.order.domain.service;

import com.saga.common.domain.valueobject.InventoryStatus;
import com.saga.common.domain.valueobject.PaymentStatus;
import com.saga.order.domain.entity.Order;
import com.saga.order.domain.event.OrderCancelledEvent;
import com.saga.order.domain.event.OrderCompletedEvent;
import com.saga.order.domain.event.OrderCreatedEvent;

import java.util.List;
import java.util.Optional;

public interface OrderDomainService {

    OrderCreatedEvent validateAndInitiateOrder(Order order);

    void updateOrderPaymentStatus(Order order, PaymentStatus paymentStatus, List<String> failureMessages);

    void updateOrderInventoryStatus(Order order, InventoryStatus inventoryStatus, List<String> failureMessages);

    Optional<OrderCompletedEvent> completeOrderSaga(Order order);

    Optional<OrderCancelledEvent> cancelOrderSaga(Order order);
}
