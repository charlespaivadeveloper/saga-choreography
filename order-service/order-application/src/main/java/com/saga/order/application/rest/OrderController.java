package com.saga.order.application.rest;

import com.saga.order.service.dto.create.CreateOrderCommand;
import com.saga.order.service.dto.create.CreateOrderResponse;
import com.saga.order.service.dto.query.GetAllOrdersResponse;
import com.saga.order.service.ports.input.service.OrderApplicationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderApplicationService orderApplicationService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderCommand createOrderCommand) {
        log.info("Creating order for userId: {} productId: {}",
            createOrderCommand.getUserId(), createOrderCommand.getProductId());
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderCommand);
        log.info("Order created with id: {}", response.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<GetAllOrdersResponse> getAllOrders() {
        GetAllOrdersResponse response = orderApplicationService.getAllOrders();
        return ResponseEntity.ok(response);
    }
}
