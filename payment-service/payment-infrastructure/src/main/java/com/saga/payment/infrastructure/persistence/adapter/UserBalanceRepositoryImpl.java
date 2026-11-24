package com.saga.payment.infrastructure.persistence.adapter;

import com.saga.common.domain.valueobject.UserId;
import com.saga.payment.domain.entity.UserBalance;
import com.saga.payment.infrastructure.persistence.mapper.UserBalancePersistenceMapper;
import com.saga.payment.infrastructure.persistence.repository.UserBalanceJpaRepository;
import com.saga.payment.service.ports.output.repository.UserBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserBalanceRepositoryImpl implements UserBalanceRepository {

    private final UserBalanceJpaRepository jpaRepository;
    private final UserBalancePersistenceMapper userBalancePersistenceMapper;

    @Override
    public UserBalance save(UserBalance userBalance) {
        return userBalancePersistenceMapper.toDomainEntity(
            jpaRepository.save(userBalancePersistenceMapper.toJpaEntity(userBalance)));
    }

    @Override
    public Optional<UserBalance> findByUserId(UserId userId) {
        return jpaRepository.findById(userId.getValue())
            .map(userBalancePersistenceMapper::toDomainEntity);
    }
}
