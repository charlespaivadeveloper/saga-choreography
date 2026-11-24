package com.saga.inventory.domain.service;

import com.saga.common.domain.valueobject.OrderId;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.OrderInventory;
import com.saga.inventory.domain.entity.OrderInventoryConsumption;
import com.saga.inventory.domain.event.InventoryRejectedEvent;
import com.saga.inventory.domain.event.InventoryReservedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

@DisplayName("InventoryDomainService")
class InventoryDomainServiceImplTest {

    private InventoryDomainService inventoryDomainService;

    private OrderId orderId;
    private ProductId productId;

    @BeforeEach
    void setUp() {
        inventoryDomainService = new InventoryDomainServiceImpl();
        orderId = OrderId.of(UUID.randomUUID());
        productId = new ProductId(1L);
    }

    @Nested
    @DisplayName("reserveInventory")
    class ReserveInventory {

        @Test
        @DisplayName("returns InventoryReservedEvent when stock is available")
        void returnsReservedEventWhenStockAvailable() {
            // Given
            var inventory = OrderInventory.builder()
                .productId(productId)
                .availableQuantity(5)
                .build();

            // When
            InventoryReservedEvent result = inventoryDomainService.reserveInventory(orderId, productId, inventory);

            // Then
            then(result).isNotNull();
            then(result.getOrderId()).isEqualTo(orderId);
            then(result.getProductId()).isEqualTo(productId);
            then(result.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("decrements quantity by one on successful reservation")
        void decrementsQuantityOnSuccess() {
            // Given
            var inventory = OrderInventory.builder()
                .productId(productId)
                .availableQuantity(5)
                .build();

            // When
            inventoryDomainService.reserveInventory(orderId, productId, inventory);

            // Then
            then(inventory.getAvailableQuantity()).isEqualTo(4);
        }

        @Test
        @DisplayName("returns null when inventory is exhausted")
        void returnsNullWhenInventoryExhausted() {
            // Given
            var inventory = OrderInventory.builder()
                .productId(productId)
                .availableQuantity(0)
                .build();

            // When
            InventoryReservedEvent result = inventoryDomainService.reserveInventory(orderId, productId, inventory);

            // Then
            then(result).isNull();
        }

        @Test
        @DisplayName("does not decrement quantity when reservation fails")
        void doesNotDecrementOnFailure() {
            // Given
            var inventory = OrderInventory.builder()
                .productId(productId)
                .availableQuantity(0)
                .build();

            // When
            inventoryDomainService.reserveInventory(orderId, productId, inventory);

            // Then
            then(inventory.getAvailableQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("succeeds with exactly one unit remaining")
        void succeedsWithOneUnitRemaining() {
            // Given
            var inventory = OrderInventory.builder()
                .productId(productId)
                .availableQuantity(1)
                .build();

            // When
            InventoryReservedEvent result = inventoryDomainService.reserveInventory(orderId, productId, inventory);

            // Then
            then(result).isNotNull();
            then(inventory.getAvailableQuantity()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("rejectInventory")
    class RejectInventory {

        @Test
        @DisplayName("returns InventoryRejectedEvent with failure message")
        void returnsRejectedEventWithFailureMessage() {
            // Given
            var inventory = OrderInventory.builder()
                .productId(productId)
                .availableQuantity(4)
                .build();
            var consumption = OrderInventoryConsumption.builder()
                .orderId(orderId)
                .productId(productId)
                .build();

            // When
            InventoryRejectedEvent result = inventoryDomainService.rejectInventory(orderId, productId, inventory, consumption);

            // Then
            then(result).isNotNull();
            then(result.getOrderId()).isEqualTo(orderId);
            then(result.getProductId()).isEqualTo(productId);
            then(result.getFailureMessages()).isNotEmpty();
        }

        @Test
        @DisplayName("restores inventory quantity on rejection")
        void restoresQuantityOnRejection() {
            // Given
            var inventory = OrderInventory.builder()
                .productId(productId)
                .availableQuantity(4)
                .build();
            var consumption = OrderInventoryConsumption.builder()
                .orderId(orderId)
                .productId(productId)
                .build();

            // When
            inventoryDomainService.rejectInventory(orderId, productId, inventory, consumption);

            // Then
            then(inventory.getAvailableQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("does not throw when processing valid restore")
        void doesNotThrowOnValidRestore() {
            // Given
            var inventory = OrderInventory.builder()
                .productId(productId)
                .availableQuantity(0)
                .build();
            var consumption = OrderInventoryConsumption.builder()
                .orderId(orderId)
                .productId(productId)
                .build();

            // When / Then
            thenNoException().isThrownBy(() ->
                inventoryDomainService.rejectInventory(orderId, productId, inventory, consumption));
        }
    }
}
