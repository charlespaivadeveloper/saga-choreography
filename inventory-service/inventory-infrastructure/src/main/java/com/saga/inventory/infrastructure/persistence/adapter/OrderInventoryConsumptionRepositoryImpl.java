package com.saga.inventory.infrastructure.persistence.adapter;

import com.saga.common.domain.valueobject.OrderId;
import com.saga.inventory.domain.entity.OrderInventoryConsumption;
import com.saga.inventory.infrastructure.persistence.mapper.OrderInventoryConsumptionPersistenceMapper;
import com.saga.inventory.infrastructure.persistence.repository.OrderInventoryConsumptionJpaRepository;
import com.saga.inventory.service.ports.output.repository.OrderInventoryConsumptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderInventoryConsumptionRepositoryImpl implements OrderInventoryConsumptionRepository {

    private final OrderInventoryConsumptionJpaRepository jpaRepository;
    private final OrderInventoryConsumptionPersistenceMapper orderInventoryConsumptionPersistenceMapper;

    @Override
    public OrderInventoryConsumption save(OrderInventoryConsumption consumption) {
        return orderInventoryConsumptionPersistenceMapper.toDomainEntity(
            jpaRepository.save(orderInventoryConsumptionPersistenceMapper.toJpaEntity(consumption)));
    }

    @Override
    public Optional<OrderInventoryConsumption> findByOrderId(OrderId orderId) {
        return jpaRepository.findByOrderId(orderId.getValue())
            .map(orderInventoryConsumptionPersistenceMapper::toDomainEntity);
    }

    @Override
    public void delete(OrderInventoryConsumption consumption) {
        jpaRepository.deleteById(consumption.getId());
    }
}
