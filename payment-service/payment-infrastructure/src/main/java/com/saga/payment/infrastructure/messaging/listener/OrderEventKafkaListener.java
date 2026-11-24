package com.saga.payment.infrastructure.messaging.listener;

import com.saga.common.application.events.OrderEventMessage;
import com.saga.payment.service.ports.input.message.listener.OrderEventMessageListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventKafkaListener.class);

    private final OrderEventMessageListener orderEventMessageListener;

    @KafkaListener(
        topics = "${payment.kafka.topic.order-events}",
        groupId = "${payment.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory")
    public void receive(@Payload OrderEventMessage message,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        MDC.put("orderId", message.getOrderId());
        MDC.put("sagaId", message.getSagaId());
        try {
            log.info("Received OrderEventMessage status: {} partition: {} offset: {}",
                message.getOrderStatus(), partition, offset);

            switch (message.getOrderStatus()) {
                case "ORDER_CREATED" -> orderEventMessageListener.processOrderCreated(message);
                case "ORDER_CANCELLED" -> orderEventMessageListener.processOrderCancelled(message);
                default -> log.debug("Ignoring order event with status: {}", message.getOrderStatus());
            }
        } finally {
            MDC.clear();
        }
    }
}
