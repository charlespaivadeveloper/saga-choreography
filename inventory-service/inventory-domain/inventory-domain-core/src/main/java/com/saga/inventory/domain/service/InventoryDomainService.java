package com.saga.inventory.domain.service;

import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.OrderInventory;
import com.saga.inventory.domain.entity.OrderInventoryConsumption;
import com.saga.inventory.domain.event.InventoryRejectedEvent;
import com.saga.inventory.domain.event.InventoryReservedEvent;

public interface InventoryDomainService {

    InventoryReservedEvent reserveInventory(OrderId orderId, ProductId productId,
                                             OrderInventory orderInventory);

    InventoryRejectedEvent rejectInventory(OrderId orderId, ProductId productId,
                                            OrderInventory orderInventory,
                                            OrderInventoryConsumption consumption);
}
