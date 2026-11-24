package com.saga.payment.service.ports.output.message.publisher;

import com.saga.common.domain.event.DomainEventPublisher;
import com.saga.payment.domain.event.PaymentRejectedEvent;

public interface PaymentRejectedMessagePublisher extends DomainEventPublisher<PaymentRejectedEvent> {
}
