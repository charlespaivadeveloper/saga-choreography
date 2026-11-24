package com.saga.order.service.ports.output.message.publisher;

import com.saga.common.domain.event.DomainEventPublisher;
import com.saga.order.domain.event.OrderCreatedEvent;

public interface OrderCreatedMessagePublisher extends DomainEventPublisher<OrderCreatedEvent> {
}
