package com.saga.payment.infrastructure.messaging.publisher;

import com.saga.common.application.events.PaymentEventMessage;
import com.saga.payment.domain.event.PaymentRejectedEvent;
import com.saga.payment.domain.event.PaymentReservedEvent;
import com.saga.payment.service.ports.output.message.publisher.PaymentRejectedMessagePublisher;
import com.saga.payment.service.ports.output.message.publisher.PaymentReservedMessagePublisher;
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
public class PaymentKafkaMessagePublisher implements PaymentReservedMessagePublisher, PaymentRejectedMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaMessagePublisher.class);

    private final KafkaTemplate<String, PaymentEventMessage> kafkaTemplate;

    @Value("${payment.kafka.topic.payment-events}")
    private String paymentEventsTopic;

    @Override
    public void publish(PaymentReservedEvent domainEvent) {
        String orderId = domainEvent.getOrderId().getValue().toString();
        PaymentEventMessage message = PaymentEventMessage.builder()
            .paymentId(UUID.randomUUID().toString())
            .orderId(orderId)
            .userId(domainEvent.getUserId().getValue())
            .amount(domainEvent.getAmount().getAmount())
            .paymentStatus("RESERVED")
            .createdAt(Instant.now())
            .build();
        kafkaTemplate.send(paymentEventsTopic, orderId, message);
        log.info("PaymentReservedEvent published for orderId: {}", orderId);
    }

    @Override
    public void publish(PaymentRejectedEvent domainEvent) {
        String orderId = domainEvent.getOrderId().getValue().toString();
        PaymentEventMessage message = PaymentEventMessage.builder()
            .paymentId(UUID.randomUUID().toString())
            .orderId(orderId)
            .userId(domainEvent.getUserId().getValue())
            .paymentStatus("REJECTED")
            .failureMessages(domainEvent.getFailureMessages())
            .createdAt(Instant.now())
            .build();
        kafkaTemplate.send(paymentEventsTopic, orderId, message);
        log.info("PaymentRejectedEvent published for orderId: {}", orderId);
    }
}
