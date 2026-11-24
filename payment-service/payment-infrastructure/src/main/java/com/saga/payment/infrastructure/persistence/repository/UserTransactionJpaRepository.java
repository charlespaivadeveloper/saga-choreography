package com.saga.payment.infrastructure.persistence.repository;

import com.saga.payment.infrastructure.persistence.entity.UserTransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTransactionJpaRepository extends JpaRepository<UserTransactionJpaEntity, UUID> {

    Optional<UserTransactionJpaEntity> findByOrderId(UUID orderId);
}
