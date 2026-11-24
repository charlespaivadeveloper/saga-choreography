package com.saga.payment.service.ports.input.message.listener;

import com.saga.common.application.events.OrderEventMessage;

public interface OrderEventMessageListener {

    void processOrderCreated(OrderEventMessage orderEventMessage);

    void processOrderCancelled(OrderEventMessage orderEventMessage);
}
