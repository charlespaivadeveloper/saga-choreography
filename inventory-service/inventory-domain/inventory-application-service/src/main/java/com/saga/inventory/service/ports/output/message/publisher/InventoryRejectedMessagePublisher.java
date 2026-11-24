package com.saga.inventory.service.ports.output.message.publisher;

import com.saga.common.domain.event.DomainEventPublisher;
import com.saga.inventory.domain.event.InventoryRejectedEvent;

public interface InventoryRejectedMessagePublisher extends DomainEventPublisher<InventoryRejectedEvent> {
}
