package com.saga.payment.infrastructure.persistence.mapper;

import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.UserId;
import com.saga.payment.domain.entity.UserBalance;
import com.saga.payment.infrastructure.persistence.entity.UserBalanceJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserBalancePersistenceMapper {

    public UserBalanceJpaEntity toJpaEntity(UserBalance userBalance) {
        return UserBalanceJpaEntity.builder()
            .userId(userBalance.getId().getValue())
            .balance(userBalance.getBalance().getAmount())
            .build();
    }

    public UserBalance toDomainEntity(UserBalanceJpaEntity entity) {
        return UserBalance.builder()
            .userId(UserId.of(entity.getUserId()))
            .balance(Money.of(entity.getBalance()))
            .build();
    }
}
