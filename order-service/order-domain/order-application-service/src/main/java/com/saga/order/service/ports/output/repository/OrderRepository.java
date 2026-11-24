package com.saga.order.service.ports.output.repository;

import com.saga.common.domain.valueobject.OrderId;
import com.saga.order.domain.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId orderId);

    List<Order> findAll();
}
