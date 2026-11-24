package com.saga.inventory.service.ports.output.repository;

import com.saga.common.domain.valueobject.OrderId;
import com.saga.inventory.domain.entity.OrderInventoryConsumption;

import java.util.Optional;

public interface OrderInventoryConsumptionRepository {

    OrderInventoryConsumption save(OrderInventoryConsumption consumption);

    Optional<OrderInventoryConsumption> findByOrderId(OrderId orderId);

    void delete(OrderInventoryConsumption consumption);
}
