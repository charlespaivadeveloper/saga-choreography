package com.saga.inventory.service;

import com.saga.common.application.events.OrderEventMessage;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.OrderInventory;
import com.saga.inventory.domain.entity.OrderInventoryConsumption;
import com.saga.inventory.domain.event.InventoryRejectedEvent;
import com.saga.inventory.domain.event.InventoryReservedEvent;
import com.saga.inventory.domain.exception.InventoryDomainException;
import com.saga.inventory.domain.service.InventoryDomainService;
import com.saga.inventory.service.ports.input.message.listener.OrderEventMessageListener;
import com.saga.inventory.service.ports.output.message.publisher.InventoryRejectedMessagePublisher;
import com.saga.inventory.service.ports.output.message.publisher.InventoryReservedMessagePublisher;
import com.saga.inventory.service.ports.output.repository.OrderInventoryConsumptionRepository;
import com.saga.inventory.service.ports.output.repository.OrderInventoryRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Observed(name = "saga.inventory.process", contextualName = "inventory-process")
public class OrderEventMessageListenerImpl implements OrderEventMessageListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventMessageListenerImpl.class);

    private final InventoryDomainService inventoryDomainService;
    private final OrderInventoryRepository orderInventoryRepository;
    private final OrderInventoryConsumptionRepository consumptionRepository;
    private final InventoryReservedMessagePublisher inventoryReservedPublisher;
    private final InventoryRejectedMessagePublisher inventoryRejectedPublisher;
    private final SagaInventoryMetrics sagaInventoryMetrics;

    @Override
    @Transactional
    public void processOrderCreated(OrderEventMessage message) {
        OrderId orderId = OrderId.of(message.getOrderId());
        ProductId productId = ProductId.of(message.getProductId());

        OrderInventory orderInventory = orderInventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new InventoryDomainException("Product not found: " + productId.getValue()));

        try {
            InventoryReservedEvent event = inventoryDomainService.reserveInventory(
                orderId, productId, orderInventory);

            if (event != null) {
                OrderInventoryConsumption consumption = OrderInventoryConsumption.builder()
                    .productId(productId)
                    .orderId(orderId)
                    .quantityConsumed(1)
                    .build();
                orderInventoryRepository.save(orderInventory);
                consumptionRepository.save(consumption);
                inventoryReservedPublisher.publish(event);
                sagaInventoryMetrics.inventoryReserved();
                log.info("Inventory reserved for orderId: {}", message.getOrderId());
            } else {
                InventoryRejectedEvent rejectedEvent = new InventoryRejectedEvent(
                    orderId, productId, List.of("Out of stock"), Instant.now());
                inventoryRejectedPublisher.publish(rejectedEvent);
                sagaInventoryMetrics.inventoryRejected();
                log.warn("Inventory rejected (out of stock) for orderId: {}", message.getOrderId());
            }
        } catch (InventoryDomainException ex) {
            InventoryRejectedEvent rejectedEvent = new InventoryRejectedEvent(
                orderId, productId, List.of(ex.getMessage()), Instant.now());
            inventoryRejectedPublisher.publish(rejectedEvent);
            sagaInventoryMetrics.inventoryRejected();
        }
    }

    @Override
    @Transactional
    public void processOrderCancelled(OrderEventMessage message) {
        OrderId orderId = OrderId.of(message.getOrderId());
        ProductId productId = ProductId.of(message.getProductId());

        consumptionRepository.findByOrderId(orderId).ifPresent(consumption -> {
            OrderInventory orderInventory = orderInventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryDomainException("Product not found: " + productId.getValue()));

            inventoryDomainService.rejectInventory(orderId, productId, orderInventory, consumption);
            orderInventoryRepository.save(orderInventory);
            consumptionRepository.delete(consumption);
            sagaInventoryMetrics.inventoryRestored();
            log.info("Inventory restored for orderId: {}", message.getOrderId());
        });
    }
}
