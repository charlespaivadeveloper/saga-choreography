package com.saga.order.infrastructure.messaging.listener;

import com.saga.common.application.events.InventoryEventMessage;
import com.saga.order.service.ports.input.message.listener.inventory.InventoryResponseMessageListener;
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
public class InventoryResponseKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryResponseKafkaListener.class);

    private final InventoryResponseMessageListener inventoryResponseMessageListener;

    @KafkaListener(
        topics = "${order.kafka.topic.inventory-events}",
        groupId = "${order.kafka.consumer.inventory-group-id}",
        containerFactory = "kafkaListenerContainerFactory")
    public void receive(@Payload InventoryEventMessage message,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        MDC.put("orderId", message.getOrderId());
        try {
            log.info("Received InventoryEventMessage status: {} partition: {} offset: {}",
                message.getInventoryStatus(), partition, offset);

            if ("RESERVED".equals(message.getInventoryStatus())) {
                inventoryResponseMessageListener.inventoryReserved(message);
            } else {
                inventoryResponseMessageListener.inventoryRejected(message);
            }
        } finally {
            MDC.clear();
        }
    }
}
