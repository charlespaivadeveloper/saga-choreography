package com.saga.order.service;

import com.saga.common.application.events.InventoryEventMessage;
import com.saga.common.domain.valueobject.InventoryStatus;
import com.saga.order.domain.entity.Order;
import com.saga.order.domain.exception.OrderNotFoundException;
import com.saga.order.domain.service.OrderDomainService;
import com.saga.order.service.ports.input.message.listener.inventory.InventoryResponseMessageListener;
import com.saga.order.service.ports.output.message.publisher.OrderCancelledMessagePublisher;
import com.saga.order.service.ports.output.message.publisher.OrderCompletedMessagePublisher;
import com.saga.order.service.ports.output.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryResponseMessageListenerImpl implements InventoryResponseMessageListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryResponseMessageListenerImpl.class);

    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final OrderCancelledMessagePublisher orderCancelledMessagePublisher;
    private final OrderCompletedMessagePublisher orderCompletedMessagePublisher;

    @Override
    @Transactional
    public void inventoryReserved(InventoryEventMessage message) {
        Order order = findOrderById(message.getOrderId());
        orderDomainService.updateOrderInventoryStatus(order, InventoryStatus.RESERVED, Collections.emptyList());
        orderRepository.save(order);
        checkAndAdvanceSaga(order);
        log.info("Inventory reserved for order: {}", message.getOrderId());
    }

    @Override
    @Transactional
    public void inventoryRejected(InventoryEventMessage message) {
        Order order = findOrderById(message.getOrderId());
        orderDomainService.updateOrderInventoryStatus(
            order,
            InventoryStatus.REJECTED,
            Optional.ofNullable(message.getFailureMessages()).orElse(Collections.emptyList()));
        orderRepository.save(order);
        checkAndAdvanceSaga(order);
        log.info("Inventory rejected for order: {}", message.getOrderId());
    }

    private Order findOrderById(String orderId) {
        return orderRepository.findById(com.saga.common.domain.valueobject.OrderId.of(orderId))
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    private void checkAndAdvanceSaga(Order order) {
        orderDomainService.completeOrderSaga(order).ifPresent(event -> {
            orderRepository.save(order);
            orderCompletedMessagePublisher.publish(event);
        });
        orderDomainService.cancelOrderSaga(order).ifPresent(event -> {
            orderRepository.save(order);
            orderCancelledMessagePublisher.publish(event);
        });
    }
}
