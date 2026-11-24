package com.saga.order.infrastructure.messaging.publisher;

import com.saga.common.application.events.OrderEventMessage;
import com.saga.order.domain.event.OrderCancelledEvent;
import com.saga.order.domain.event.OrderCompletedEvent;
import com.saga.order.domain.event.OrderCreatedEvent;
import com.saga.order.service.ports.output.message.publisher.OrderCancelledMessagePublisher;
import com.saga.order.service.ports.output.message.publisher.OrderCompletedMessagePublisher;
import com.saga.order.service.ports.output.message.publisher.OrderCreatedMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OrderKafkaMessagePublisher
    implements OrderCreatedMessagePublisher, OrderCancelledMessagePublisher, OrderCompletedMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaMessagePublisher.class);

    private final KafkaTemplate<String, OrderEventMessage> kafkaTemplate;

    @Value("${order.kafka.topic.order-events}")
    private String orderEventsTopic;

    @Override
    public void publish(OrderCreatedEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        MDC.put("orderId", orderId);
        try {
            OrderEventMessage message = buildMessage(domainEvent.getOrder(), "ORDER_CREATED");
            sendMessage(orderId, message);
            log.info("OrderCreatedEvent published");
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void publish(OrderCancelledEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        MDC.put("orderId", orderId);
        try {
            OrderEventMessage message = buildMessage(domainEvent.getOrder(), "ORDER_CANCELLED");
            sendMessage(orderId, message);
            log.info("OrderCancelledEvent published");
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void publish(OrderCompletedEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        MDC.put("orderId", orderId);
        try {
            OrderEventMessage message = buildMessage(domainEvent.getOrder(), "ORDER_COMPLETED");
            sendMessage(orderId, message);
            log.info("OrderCompletedEvent published");
        } finally {
            MDC.clear();
        }
    }

    private OrderEventMessage buildMessage(com.saga.order.domain.entity.Order order, String status) {
        return OrderEventMessage.builder()
            .orderId(order.getId().getValue().toString())
            .userId(order.getUserId().getValue())
            .productId(order.getProductId().getValue())
            .price(order.getPrice().getAmount())
            .orderStatus(status)
            .sagaId(order.getId().getValue().toString())
            .createdAt(Instant.now())
            .build();
    }

    private void sendMessage(String key, OrderEventMessage message) {
        kafkaTemplate.send(orderEventsTopic, key, message);
    }
}
