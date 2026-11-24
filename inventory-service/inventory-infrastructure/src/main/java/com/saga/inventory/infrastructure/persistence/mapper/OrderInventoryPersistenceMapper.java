package com.saga.inventory.infrastructure.persistence.mapper;

import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.OrderInventory;
import com.saga.inventory.infrastructure.persistence.entity.OrderInventoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderInventoryPersistenceMapper {

    public OrderInventoryJpaEntity toJpaEntity(OrderInventory orderInventory) {
        return OrderInventoryJpaEntity.builder()
            .productId(orderInventory.getId().getValue())
            .availableInventory(orderInventory.getAvailableQuantity())
            .build();
    }

    public OrderInventory toDomainEntity(OrderInventoryJpaEntity entity) {
        return OrderInventory.builder()
            .productId(ProductId.of(entity.getProductId()))
            .availableQuantity(entity.getAvailableInventory())
            .build();
    }
}
