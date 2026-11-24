package com.saga.payment.infrastructure.persistence.repository;

import com.saga.payment.infrastructure.persistence.entity.UserBalanceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBalanceJpaRepository extends JpaRepository<UserBalanceJpaEntity, Long> {
}
