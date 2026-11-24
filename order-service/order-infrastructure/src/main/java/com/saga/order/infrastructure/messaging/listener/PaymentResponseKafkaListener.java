package com.saga.order.infrastructure.messaging.listener;

import com.saga.common.application.events.PaymentEventMessage;
import com.saga.order.service.ports.input.message.listener.payment.PaymentResponseMessageListener;
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
public class PaymentResponseKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentResponseKafkaListener.class);

    private final PaymentResponseMessageListener paymentResponseMessageListener;

    @KafkaListener(
        topics = "${order.kafka.topic.payment-events}",
        groupId = "${order.kafka.consumer.payment-group-id}",
        containerFactory = "kafkaListenerContainerFactory")
    public void receive(@Payload PaymentEventMessage message,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        MDC.put("orderId", message.getOrderId());
        try {
            log.info("Received PaymentEventMessage status: {} partition: {} offset: {}",
                message.getPaymentStatus(), partition, offset);

            if ("RESERVED".equals(message.getPaymentStatus())) {
                paymentResponseMessageListener.paymentCompleted(message);
            } else {
                paymentResponseMessageListener.paymentCancelled(message);
            }
        } finally {
            MDC.clear();
        }
    }
}
