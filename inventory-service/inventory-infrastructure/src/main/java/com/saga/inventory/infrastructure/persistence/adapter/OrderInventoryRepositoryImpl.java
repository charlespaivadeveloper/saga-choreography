package com.saga.inventory.infrastructure.persistence.adapter;

import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.OrderInventory;
import com.saga.inventory.infrastructure.persistence.mapper.OrderInventoryPersistenceMapper;
import com.saga.inventory.infrastructure.persistence.repository.OrderInventoryJpaRepository;
import com.saga.inventory.service.ports.output.repository.OrderInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderInventoryRepositoryImpl implements OrderInventoryRepository {

    private final OrderInventoryJpaRepository jpaRepository;
    private final OrderInventoryPersistenceMapper orderInventoryPersistenceMapper;

    @Override
    public OrderInventory save(OrderInventory orderInventory) {
        return orderInventoryPersistenceMapper.toDomainEntity(
            jpaRepository.save(orderInventoryPersistenceMapper.toJpaEntity(orderInventory)));
    }

    @Override
    public Optional<OrderInventory> findByProductId(ProductId productId) {
        return jpaRepository.findById(productId.getValue())
            .map(orderInventoryPersistenceMapper::toDomainEntity);
    }
}
