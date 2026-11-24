package com.saga.payment.domain.service;

import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.UserId;
import com.saga.payment.domain.entity.UserBalance;
import com.saga.payment.domain.entity.UserTransaction;
import com.saga.payment.domain.event.PaymentRejectedEvent;
import com.saga.payment.domain.event.PaymentReservedEvent;
import com.saga.payment.domain.exception.PaymentDomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

public class PaymentDomainServiceImpl implements PaymentDomainService {

    private static final Logger log = LoggerFactory.getLogger(PaymentDomainServiceImpl.class);

    @Override
    public PaymentReservedEvent reservePayment(OrderId orderId, UserId userId, Money amount,
                                                UserBalance userBalance) {
        try {
            userBalance.reserve(amount);
            log.info("Payment reserved for orderId: {} userId: {} amount: {}",
                orderId.getValue(), userId.getValue(), amount);
            return new PaymentReservedEvent(orderId, userId, amount, Instant.now());
        } catch (PaymentDomainException ex) {
            log.warn("Payment rejected for orderId: {} - {}", orderId.getValue(), ex.getMessage());
            return null;
        }
    }

    @Override
    public PaymentRejectedEvent rejectPayment(OrderId orderId, UserId userId,
                                               UserBalance userBalance, UserTransaction userTransaction) {
        userBalance.refund(userTransaction.getAmount());
        log.info("Payment refunded for orderId: {} userId: {} amount: {}",
            orderId.getValue(), userId.getValue(), userTransaction.getAmount());
        return new PaymentRejectedEvent(orderId, userId,
            List.of("Order cancelled - payment refunded"), Instant.now());
    }
}
