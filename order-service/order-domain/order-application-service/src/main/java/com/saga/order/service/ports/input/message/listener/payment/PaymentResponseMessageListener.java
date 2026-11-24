package com.saga.order.service.ports.input.message.listener.payment;

import com.saga.common.application.events.PaymentEventMessage;

public interface PaymentResponseMessageListener {

    void paymentCompleted(PaymentEventMessage paymentEventMessage);

    void paymentCancelled(PaymentEventMessage paymentEventMessage);
}
