package com.saga.order.infrastructure.persistence.mapper;

import com.saga.common.domain.valueobject.InventoryStatus;
import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.OrderStatus;
import com.saga.common.domain.valueobject.PaymentStatus;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.common.domain.valueobject.UserId;
import com.saga.order.domain.entity.Order;
import com.saga.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderPersistenceMapper {

    private static final String FAILURE_MESSAGES_DELIMITER = ";";

    public OrderJpaEntity toJpaEntity(Order order) {
        return OrderJpaEntity.builder()
            .id(order.getId().getValue())
            .userId(order.getUserId().getValue())
            .productId(order.getProductId().getValue())
            .price(order.getPrice().getAmount())
            .orderStatus(order.getOrderStatus())
            .paymentStatus(order.getPaymentStatus())
            .inventoryStatus(order.getInventoryStatus())
            .failureMessages(joinFailureMessages(order.getFailureMessages()))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    public Order toDomainEntity(OrderJpaEntity entity) {
        return Order.builder()
            .orderId(OrderId.of(entity.getId()))
            .userId(UserId.of(entity.getUserId()))
            .productId(ProductId.of(entity.getProductId()))
            .price(Money.of(entity.getPrice()))
            .orderStatus(entity.getOrderStatus())
            .paymentStatus(entity.getPaymentStatus())
            .inventoryStatus(entity.getInventoryStatus())
            .failureMessages(splitFailureMessages(entity.getFailureMessages()))
            .build();
    }

    private String joinFailureMessages(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        return messages.stream()
            .filter(msg -> msg != null && !msg.isBlank())
            .collect(Collectors.joining(FAILURE_MESSAGES_DELIMITER));
    }

    private List<String> splitFailureMessages(String failureMessages) {
        return Optional.ofNullable(failureMessages)
            .filter(msg -> !msg.isBlank())
            .map(msg -> Arrays.asList(msg.split(FAILURE_MESSAGES_DELIMITER)))
            .orElse(Collections.emptyList());
    }
}
