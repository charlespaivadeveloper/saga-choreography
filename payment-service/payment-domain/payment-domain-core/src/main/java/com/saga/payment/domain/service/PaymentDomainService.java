package com.saga.payment.domain.service;

import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.UserId;
import com.saga.payment.domain.entity.UserBalance;
import com.saga.payment.domain.entity.UserTransaction;
import com.saga.payment.domain.event.PaymentRejectedEvent;
import com.saga.payment.domain.event.PaymentReservedEvent;

public interface PaymentDomainService {

    PaymentReservedEvent reservePayment(OrderId orderId, UserId userId, Money amount,
                                        UserBalance userBalance);

    PaymentRejectedEvent rejectPayment(OrderId orderId, UserId userId,
                                       UserBalance userBalance, UserTransaction userTransaction);
}
