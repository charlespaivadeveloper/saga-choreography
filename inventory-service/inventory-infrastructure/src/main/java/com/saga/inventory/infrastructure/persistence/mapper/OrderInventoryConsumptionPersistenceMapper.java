package com.saga.inventory.infrastructure.persistence.mapper;

import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.OrderInventoryConsumption;
import com.saga.inventory.infrastructure.persistence.entity.OrderInventoryConsumptionJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderInventoryConsumptionPersistenceMapper {

    public OrderInventoryConsumptionJpaEntity toJpaEntity(OrderInventoryConsumption consumption) {
        return OrderInventoryConsumptionJpaEntity.builder()
            .id(consumption.getId())
            .productId(consumption.getProductId().getValue())
            .orderId(consumption.getOrderId().getValue())
            .quantityConsumed(consumption.getQuantityConsumed())
            .createdAt(Instant.now())
            .build();
    }

    public OrderInventoryConsumption toDomainEntity(OrderInventoryConsumptionJpaEntity entity) {
        return OrderInventoryConsumption.builder()
            .consumptionId(entity.getId())
            .productId(ProductId.of(entity.getProductId()))
            .orderId(OrderId.of(entity.getOrderId()))
            .quantityConsumed(entity.getQuantityConsumed())
            .build();
    }
}
