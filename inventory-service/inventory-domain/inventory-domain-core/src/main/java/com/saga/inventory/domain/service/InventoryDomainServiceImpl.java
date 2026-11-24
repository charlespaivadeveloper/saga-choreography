package com.saga.inventory.domain.service;

import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.OrderInventory;
import com.saga.inventory.domain.entity.OrderInventoryConsumption;
import com.saga.inventory.domain.event.InventoryRejectedEvent;
import com.saga.inventory.domain.event.InventoryReservedEvent;
import com.saga.inventory.domain.exception.InventoryDomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

public class InventoryDomainServiceImpl implements InventoryDomainService {

    private static final Logger log = LoggerFactory.getLogger(InventoryDomainServiceImpl.class);

    @Override
    public InventoryReservedEvent reserveInventory(OrderId orderId, ProductId productId,
                                                    OrderInventory orderInventory) {
        try {
            orderInventory.reserve();
            log.info("Inventory reserved for orderId: {} productId: {}",
                orderId.getValue(), productId.getValue());
            return new InventoryReservedEvent(orderId, productId, Instant.now());
        } catch (InventoryDomainException ex) {
            log.warn("Inventory rejected for orderId: {} - {}", orderId.getValue(), ex.getMessage());
            return null;
        }
    }

    @Override
    public InventoryRejectedEvent rejectInventory(OrderId orderId, ProductId productId,
                                                    OrderInventory orderInventory,
                                                    OrderInventoryConsumption consumption) {
        orderInventory.restore();
        log.info("Inventory restored for orderId: {} productId: {}",
            orderId.getValue(), productId.getValue());
        return new InventoryRejectedEvent(orderId, productId,
            List.of("Order cancelled - inventory restored"), Instant.now());
    }
}
