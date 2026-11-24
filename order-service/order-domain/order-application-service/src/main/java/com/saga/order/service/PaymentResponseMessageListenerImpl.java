package com.saga.order.service;

import com.saga.common.application.events.PaymentEventMessage;
import com.saga.common.domain.valueobject.PaymentStatus;
import com.saga.order.domain.entity.Order;
import com.saga.order.domain.exception.OrderNotFoundException;
import com.saga.order.domain.service.OrderDomainService;
import com.saga.order.service.ports.input.message.listener.payment.PaymentResponseMessageListener;
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
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentResponseMessageListenerImpl.class);

    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final OrderCancelledMessagePublisher orderCancelledMessagePublisher;
    private final OrderCompletedMessagePublisher orderCompletedMessagePublisher;

    @Override
    @Transactional
    public void paymentCompleted(PaymentEventMessage message) {
        Order order = findOrderById(message.getOrderId());
        orderDomainService.updateOrderPaymentStatus(order, PaymentStatus.RESERVED, Collections.emptyList());
        orderRepository.save(order);
        checkAndAdvanceSaga(order);
        log.info("Payment completed for order: {}", message.getOrderId());
    }

    @Override
    @Transactional
    public void paymentCancelled(PaymentEventMessage message) {
        Order order = findOrderById(message.getOrderId());
        orderDomainService.updateOrderPaymentStatus(
            order,
            PaymentStatus.REJECTED,
            Optional.ofNullable(message.getFailureMessages()).orElse(Collections.emptyList()));
        orderRepository.save(order);
        checkAndAdvanceSaga(order);
        log.info("Payment cancelled for order: {}", message.getOrderId());
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
