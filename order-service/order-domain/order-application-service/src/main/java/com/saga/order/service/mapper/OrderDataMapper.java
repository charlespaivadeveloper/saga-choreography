package com.saga.order.service.mapper;

import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.common.domain.valueobject.UserId;
import com.saga.order.domain.entity.Order;
import com.saga.order.service.dto.create.CreateOrderCommand;
import com.saga.order.service.dto.create.CreateOrderResponse;
import com.saga.order.service.dto.query.OrderDto;
import org.springframework.stereotype.Component;

@Component
public class OrderDataMapper {

    public Order createOrderCommandToOrder(CreateOrderCommand command, Money price) {
        return Order.builder()
            .userId(UserId.of(command.getUserId()))
            .productId(ProductId.of(command.getProductId()))
            .price(price)
            .build();
    }

    public CreateOrderResponse orderToCreateOrderResponse(Order order, String message) {
        return CreateOrderResponse.builder()
            .orderId(order.getId().getValue())
            .orderStatus(order.getOrderStatus())
            .message(message)
            .build();
    }

    public OrderDto orderToOrderDto(Order order) {
        return OrderDto.builder()
            .orderId(order.getId().getValue())
            .userId(order.getUserId().getValue())
            .productId(order.getProductId().getValue())
            .price(order.getPrice().getAmount())
            .orderStatus(order.getOrderStatus())
            .paymentStatus(order.getPaymentStatus())
            .inventoryStatus(order.getInventoryStatus())
            .failureMessages(order.getFailureMessages())
            .build();
    }

    public OrderId toOrderId(String orderId) {
        return OrderId.of(orderId);
    }
}
