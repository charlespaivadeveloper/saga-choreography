package com.saga.order.domain.service;

import com.saga.common.domain.valueobject.InventoryStatus;
import com.saga.common.domain.valueobject.Money;
import com.saga.common.domain.valueobject.OrderStatus;
import com.saga.common.domain.valueobject.PaymentStatus;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.common.domain.valueobject.UserId;
import com.saga.order.domain.entity.Order;
import com.saga.order.domain.event.OrderCancelledEvent;
import com.saga.order.domain.event.OrderCompletedEvent;
import com.saga.order.domain.event.OrderCreatedEvent;
import com.saga.order.domain.exception.OrderDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

@DisplayName("Order Domain Service")
class OrderDomainServiceImplTest {

    private OrderDomainService orderDomainService;

    @BeforeEach
    void setUp() {
        orderDomainService = new OrderDomainServiceImpl();
    }

    private Order buildValidOrder() {
        return Order.builder()
            .userId(UserId.of(1L))
            .productId(ProductId.of(1L))
            .price(Money.of("100.00"))
            .build();
    }

    @Nested
    @DisplayName("validateAndInitiateOrder")
    class ValidateAndInitiateOrder {

        @Test
        @DisplayName("should initialize order with ORDER_CREATED status when order is valid")
        void shouldInitializeOrderWhenValid() {
            // Given
            Order order = buildValidOrder();

            // When
            OrderCreatedEvent event = orderDomainService.validateAndInitiateOrder(order);

            // Then
            then(event).isNotNull();
            then(order.getId()).isNotNull();
            then(order.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CREATED);
            then(event.getOrder()).isEqualTo(order);
            then(event.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when price is zero")
        void shouldThrowExceptionWhenPriceIsZero() {
            // Given
            Order order = Order.builder()
                .userId(UserId.of(1L))
                .productId(ProductId.of(1L))
                .price(Money.ZERO)
                .build();

            // When & Then
            thenThrownBy(() -> orderDomainService.validateAndInitiateOrder(order))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("price must be greater than zero");
        }

        @Test
        @DisplayName("should throw exception when userId is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // Given
            Order order = Order.builder()
                .productId(ProductId.of(1L))
                .price(Money.of("100.00"))
                .build();

            // When & Then
            thenThrownBy(() -> orderDomainService.validateAndInitiateOrder(order))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("userId cannot be null");
        }
    }

    @Nested
    @DisplayName("completeOrderSaga")
    class CompleteOrderSaga {

        @Test
        @DisplayName("should complete order when both payment and inventory are reserved")
        void shouldCompleteOrderWhenBothReserved() {
            // Given
            Order order = buildValidOrder();
            orderDomainService.validateAndInitiateOrder(order);
            orderDomainService.updateOrderPaymentStatus(order, PaymentStatus.RESERVED, Collections.emptyList());
            orderDomainService.updateOrderInventoryStatus(order, InventoryStatus.RESERVED, Collections.emptyList());

            // When
            Optional<OrderCompletedEvent> event = orderDomainService.completeOrderSaga(order);

            // Then
            then(event).isPresent();
            then(order.getOrderStatus()).isEqualTo(OrderStatus.ORDER_COMPLETED);
        }

        @Test
        @DisplayName("should not complete order when payment is rejected")
        void shouldNotCompleteOrderWhenPaymentRejected() {
            // Given
            Order order = buildValidOrder();
            orderDomainService.validateAndInitiateOrder(order);
            orderDomainService.updateOrderPaymentStatus(order, PaymentStatus.REJECTED, List.of("Insufficient balance"));
            orderDomainService.updateOrderInventoryStatus(order, InventoryStatus.RESERVED, Collections.emptyList());

            // When
            Optional<OrderCompletedEvent> event = orderDomainService.completeOrderSaga(order);

            // Then
            then(event).isEmpty();
        }
    }

    @Nested
    @DisplayName("cancelOrderSaga")
    class CancelOrderSaga {

        @Test
        @DisplayName("should cancel order when payment is rejected")
        void shouldCancelOrderWhenPaymentRejected() {
            // Given
            Order order = buildValidOrder();
            orderDomainService.validateAndInitiateOrder(order);
            orderDomainService.updateOrderPaymentStatus(order, PaymentStatus.REJECTED, List.of("Insufficient balance"));

            // When
            Optional<OrderCancelledEvent> event = orderDomainService.cancelOrderSaga(order);

            // Then
            then(event).isPresent();
            then(order.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CANCELLED);
            then(order.getFailureMessages()).contains("Insufficient balance");
        }

        @Test
        @DisplayName("should cancel order when inventory is rejected")
        void shouldCancelOrderWhenInventoryRejected() {
            // Given
            Order order = buildValidOrder();
            orderDomainService.validateAndInitiateOrder(order);
            orderDomainService.updateOrderPaymentStatus(order, PaymentStatus.RESERVED, Collections.emptyList());
            orderDomainService.updateOrderInventoryStatus(order, InventoryStatus.REJECTED, List.of("Out of stock"));

            // When
            Optional<OrderCancelledEvent> event = orderDomainService.cancelOrderSaga(order);

            // Then
            then(event).isPresent();
            then(order.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CANCELLED);
            then(order.getFailureMessages()).contains("Out of stock");
        }

        @Test
        @DisplayName("should not cancel order when both are reserved")
        void shouldNotCancelOrderWhenBothReserved() {
            // Given
            Order order = buildValidOrder();
            orderDomainService.validateAndInitiateOrder(order);
            orderDomainService.updateOrderPaymentStatus(order, PaymentStatus.RESERVED, Collections.emptyList());
            orderDomainService.updateOrderInventoryStatus(order, InventoryStatus.RESERVED, Collections.emptyList());

            // When
            Optional<OrderCancelledEvent> event = orderDomainService.cancelOrderSaga(order);

            // Then
            then(event).isEmpty();
        }
    }
}
