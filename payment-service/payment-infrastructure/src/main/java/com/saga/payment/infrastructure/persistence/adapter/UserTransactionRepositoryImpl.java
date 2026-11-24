package com.saga.payment.infrastructure.persistence.adapter;

import com.saga.common.domain.valueobject.OrderId;
import com.saga.payment.domain.entity.UserTransaction;
import com.saga.payment.infrastructure.persistence.mapper.UserTransactionPersistenceMapper;
import com.saga.payment.infrastructure.persistence.repository.UserTransactionJpaRepository;
import com.saga.payment.service.ports.output.repository.UserTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserTransactionRepositoryImpl implements UserTransactionRepository {

    private final UserTransactionJpaRepository jpaRepository;
    private final UserTransactionPersistenceMapper userTransactionPersistenceMapper;

    @Override
    public UserTransaction save(UserTransaction userTransaction) {
        return userTransactionPersistenceMapper.toDomainEntity(
            jpaRepository.save(userTransactionPersistenceMapper.toJpaEntity(userTransaction)));
    }

    @Override
    public Optional<UserTransaction> findByOrderId(OrderId orderId) {
        return jpaRepository.findByOrderId(orderId.getValue())
            .map(userTransactionPersistenceMapper::toDomainEntity);
    }

    @Override
    public void delete(UserTransaction userTransaction) {
        jpaRepository.deleteById(userTransaction.getId());
    }
}
