package com.saga.payment.service.ports.output.repository;

import com.saga.common.domain.valueobject.OrderId;
import com.saga.payment.domain.entity.UserTransaction;

import java.util.Optional;

public interface UserTransactionRepository {

    UserTransaction save(UserTransaction userTransaction);

    Optional<UserTransaction> findByOrderId(OrderId orderId);

    void delete(UserTransaction userTransaction);
}
