package com.saga.inventory.service.ports.output.repository;

import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.OrderInventory;

import java.util.Optional;

public interface OrderInventoryRepository {

    OrderInventory save(OrderInventory orderInventory);

    Optional<OrderInventory> findByProductId(ProductId productId);
}
