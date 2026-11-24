package com.saga.order.service;

import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.order.domain.entity.Order;
import com.saga.order.domain.event.OrderCreatedEvent;
import com.saga.order.domain.exception.OrderDomainException;
import com.saga.order.domain.service.OrderDomainService;
import com.saga.order.service.dto.create.CreateOrderCommand;
import com.saga.order.service.dto.create.CreateOrderResponse;
import com.saga.order.service.dto.query.GetAllOrdersResponse;
import com.saga.order.service.mapper.OrderDataMapper;
import com.saga.order.service.ports.input.service.OrderApplicationService;
import com.saga.order.service.ports.output.message.publisher.OrderCreatedMessagePublisher;
import com.saga.order.service.ports.output.repository.OrderRepository;
import com.saga.order.service.ports.output.repository.ProductCatalogRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class OrderCreateCommandHandler implements OrderApplicationService {

    private static final Logger log = LoggerFactory.getLogger(OrderCreateCommandHandler.class);

    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final ProductCatalogRepository productCatalogRepository;
    private final OrderDataMapper orderDataMapper;
    private final OrderCreatedMessagePublisher orderCreatedMessagePublisher;

    @Override
    @Transactional
    public CreateOrderResponse createOrder(@Valid CreateOrderCommand createOrderCommand) {
        Money price = productCatalogRepository
            .findPriceByProductId(ProductId.of(createOrderCommand.getProductId()))
            .orElseThrow(() -> new OrderDomainException(
                "Product not found with id: " + createOrderCommand.getProductId()));

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand, price);
        OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order);
        Order savedOrder = orderRepository.save(order);
        orderCreatedMessagePublisher.publish(orderCreatedEvent);

        log.info("Order created with id: {}", savedOrder.getId().getValue());
        return orderDataMapper.orderToCreateOrderResponse(savedOrder, "Order created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public GetAllOrdersResponse getAllOrders() {
        return GetAllOrdersResponse.builder()
            .orders(orderRepository.findAll().stream()
                .map(orderDataMapper::orderToOrderDto)
                .collect(Collectors.toList()))
            .build();
    }
}
