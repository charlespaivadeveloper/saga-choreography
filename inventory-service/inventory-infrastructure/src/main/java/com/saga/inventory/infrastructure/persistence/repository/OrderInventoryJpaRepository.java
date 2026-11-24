package com.saga.inventory.infrastructure.persistence.repository;

import com.saga.inventory.infrastructure.persistence.entity.OrderInventoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderInventoryJpaRepository extends JpaRepository<OrderInventoryJpaEntity, Long> {
}
