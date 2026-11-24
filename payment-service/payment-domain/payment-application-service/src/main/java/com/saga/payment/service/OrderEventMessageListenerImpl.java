package com.saga.payment.service;

import com.saga.common.application.events.OrderEventMessage;
import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.UserId;
import com.saga.payment.domain.entity.UserBalance;
import com.saga.payment.domain.entity.UserTransaction;
import com.saga.payment.domain.event.PaymentRejectedEvent;
import com.saga.payment.domain.event.PaymentReservedEvent;
import com.saga.payment.domain.exception.PaymentDomainException;
import com.saga.payment.domain.service.PaymentDomainService;
import com.saga.payment.service.ports.input.message.listener.OrderEventMessageListener;
import com.saga.payment.service.ports.output.message.publisher.PaymentRejectedMessagePublisher;
import com.saga.payment.service.ports.output.message.publisher.PaymentReservedMessagePublisher;
import com.saga.payment.service.ports.output.repository.UserBalanceRepository;
import com.saga.payment.service.ports.output.repository.UserTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderEventMessageListenerImpl implements OrderEventMessageListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventMessageListenerImpl.class);

    private final PaymentDomainService paymentDomainService;
    private final UserBalanceRepository userBalanceRepository;
    private final UserTransactionRepository userTransactionRepository;
    private final PaymentReservedMessagePublisher paymentReservedPublisher;
    private final PaymentRejectedMessagePublisher paymentRejectedPublisher;

    @Override
    @Transactional
    public void processOrderCreated(OrderEventMessage message) {
        OrderId orderId = OrderId.of(message.getOrderId());
        UserId userId = UserId.of(message.getUserId());
        Money amount = Money.of(message.getPrice());

        UserBalance userBalance = userBalanceRepository.findByUserId(userId)
            .orElseThrow(() -> new PaymentDomainException("User not found: " + userId.getValue()));

        try {
            PaymentReservedEvent event = paymentDomainService.reservePayment(orderId, userId, amount, userBalance);
            if (event != null) {
                UserTransaction transaction = UserTransaction.builder()
                    .userId(userId)
                    .orderId(orderId)
                    .amount(amount)
                    .build();
                userBalanceRepository.save(userBalance);
                userTransactionRepository.save(transaction);
                paymentReservedPublisher.publish(event);
                log.info("Payment reserved for orderId: {}", message.getOrderId());
            } else {
                PaymentRejectedEvent rejectedEvent = new PaymentRejectedEvent(
                    orderId, userId, List.of("Insufficient balance"), Instant.now());
                paymentRejectedPublisher.publish(rejectedEvent);
                log.warn("Payment rejected (insufficient balance) for orderId: {}", message.getOrderId());
            }
        } catch (PaymentDomainException ex) {
            PaymentRejectedEvent rejectedEvent = new PaymentRejectedEvent(
                orderId, userId, List.of(ex.getMessage()), Instant.now());
            paymentRejectedPublisher.publish(rejectedEvent);
        }
    }

    @Override
    @Transactional
    public void processOrderCancelled(OrderEventMessage message) {
        OrderId orderId = OrderId.of(message.getOrderId());
        UserId userId = UserId.of(message.getUserId());

        userTransactionRepository.findByOrderId(orderId).ifPresent(transaction -> {
            UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentDomainException("User not found: " + userId.getValue()));

            PaymentRejectedEvent event = paymentDomainService.rejectPayment(
                orderId, userId, userBalance, transaction);
            userBalanceRepository.save(userBalance);
            userTransactionRepository.delete(transaction);
            paymentRejectedPublisher.publish(event);
            log.info("Payment refunded for orderId: {}", message.getOrderId());
        });
    }
}
