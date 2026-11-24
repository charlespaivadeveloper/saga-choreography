package com.saga.payment.infrastructure.persistence.mapper;

import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.UserId;
import com.saga.payment.domain.entity.UserTransaction;
import com.saga.payment.infrastructure.persistence.entity.UserTransactionJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserTransactionPersistenceMapper {

    public UserTransactionJpaEntity toJpaEntity(UserTransaction userTransaction) {
        return UserTransactionJpaEntity.builder()
            .id(userTransaction.getId())
            .userId(userTransaction.getUserId().getValue())
            .orderId(userTransaction.getOrderId().getValue())
            .amount(userTransaction.getAmount().getAmount())
            .createdAt(Instant.now())
            .build();
    }

    public UserTransaction toDomainEntity(UserTransactionJpaEntity entity) {
        return UserTransaction.builder()
            .transactionId(entity.getId())
            .userId(UserId.of(entity.getUserId()))
            .orderId(OrderId.of(entity.getOrderId()))
            .amount(Money.of(entity.getAmount()))
            .build();
    }
}
