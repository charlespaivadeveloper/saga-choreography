package com.saga.order.infrastructure.persistence.adapter;

import com.saga.common.domain.valueobject.OrderId;
import com.saga.order.domain.entity.Order;
import com.saga.order.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.saga.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.saga.order.service.ports.output.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderPersistenceMapper orderPersistenceMapper;

    @Override
    public Order save(Order order) {
        return orderPersistenceMapper.toDomainEntity(
            orderJpaRepository.save(orderPersistenceMapper.toJpaEntity(order)));
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return orderJpaRepository.findById(orderId.getValue())
            .map(orderPersistenceMapper::toDomainEntity);
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll().stream()
            .map(orderPersistenceMapper::toDomainEntity)
            .collect(Collectors.toList());
    }
}
