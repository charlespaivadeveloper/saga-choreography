package com.saga.inventory.infrastructure.messaging.publisher;

import com.saga.common.application.events.InventoryEventMessage;
import com.saga.inventory.domain.event.InventoryRejectedEvent;
import com.saga.inventory.domain.event.InventoryReservedEvent;
import com.saga.inventory.service.ports.output.message.publisher.InventoryRejectedMessagePublisher;
import com.saga.inventory.service.ports.output.message.publisher.InventoryReservedMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryKafkaMessagePublisher
    implements InventoryReservedMessagePublisher, InventoryRejectedMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(InventoryKafkaMessagePublisher.class);

    private final KafkaTemplate<String, InventoryEventMessage> kafkaTemplate;

    @Value("${inventory.kafka.topic.inventory-events}")
    private String inventoryEventsTopic;

    @Override
    public void publish(InventoryReservedEvent domainEvent) {
        String orderId = domainEvent.getOrderId().getValue().toString();
        InventoryEventMessage message = InventoryEventMessage.builder()
            .inventoryId(UUID.randomUUID().toString())
            .orderId(orderId)
            .productId(domainEvent.getProductId().getValue())
            .inventoryStatus("RESERVED")
            .createdAt(Instant.now())
            .build();
        kafkaTemplate.send(inventoryEventsTopic, orderId, message);
        log.info("InventoryReservedEvent published for orderId: {}", orderId);
    }

    @Override
    public void publish(InventoryRejectedEvent domainEvent) {
        String orderId = domainEvent.getOrderId().getValue().toString();
        InventoryEventMessage message = InventoryEventMessage.builder()
            .inventoryId(UUID.randomUUID().toString())
            .orderId(orderId)
            .productId(domainEvent.getProductId().getValue())
            .inventoryStatus("REJECTED")
            .failureMessages(domainEvent.getFailureMessages())
            .createdAt(Instant.now())
            .build();
        kafkaTemplate.send(inventoryEventsTopic, orderId, message);
        log.info("InventoryRejectedEvent published for orderId: {}", orderId);
    }
}
