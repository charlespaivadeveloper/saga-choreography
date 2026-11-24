package com.saga.inventory.infrastructure.persistence.repository;

import com.saga.inventory.infrastructure.persistence.entity.OrderInventoryConsumptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderInventoryConsumptionJpaRepository
    extends JpaRepository<OrderInventoryConsumptionJpaEntity, UUID> {

    Optional<OrderInventoryConsumptionJpaEntity> findByOrderId(UUID orderId);
}
