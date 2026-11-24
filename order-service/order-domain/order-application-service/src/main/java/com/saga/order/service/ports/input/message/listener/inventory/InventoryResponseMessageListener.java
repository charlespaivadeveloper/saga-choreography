package com.saga.order.service.ports.input.message.listener.inventory;

import com.saga.common.application.events.InventoryEventMessage;

public interface InventoryResponseMessageListener {

    void inventoryReserved(InventoryEventMessage inventoryEventMessage);

    void inventoryRejected(InventoryEventMessage inventoryEventMessage);
}
