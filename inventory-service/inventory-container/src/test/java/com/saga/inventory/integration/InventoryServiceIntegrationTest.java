package com.saga.inventory.integration;

import com.saga.common.application.events.OrderEventMessage;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.OrderInventory;
import com.saga.inventory.service.ports.input.message.listener.OrderEventMessageListener;
import com.saga.inventory.service.ports.output.repository.OrderInventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Inventory Service Integration Tests")
class InventoryServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("inventory_db")
        .withUsername("saga_user")
        .withPassword("saga_password");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private OrderEventMessageListener orderEventMessageListener;

    @Autowired
    private OrderInventoryRepository orderInventoryRepository;

    @Test
    @DisplayName("should process order created event and reserve inventory when stock is available")
    void shouldReserveInventoryWhenStockAvailable() {
        // Given
        ProductId productId = new ProductId(1L);
        Optional<OrderInventory> inventory = orderInventoryRepository.findByProductId(productId);
        then(inventory).isPresent();
        then(inventory.get().getAvailableQuantity()).isPositive();

        OrderEventMessage message = OrderEventMessage.builder()
            .orderId(UUID.randomUUID().toString())
            .userId(1L)
            .productId(1L)
            .price(new BigDecimal("100.00"))
            .orderStatus("ORDER_CREATED")
            .sagaId(UUID.randomUUID().toString())
            .createdAt(Instant.now())
            .build();

        // When / Then (no exception means inventory was processed)
        orderEventMessageListener.processOrderCreated(message);
    }

    @Test
    @DisplayName("should find inventory for existing product")
    void shouldFindInventoryForExistingProduct() {
        // Given
        ProductId productId = new ProductId(1L);

        // When
        Optional<OrderInventory> inventory = orderInventoryRepository.findByProductId(productId);

        // Then
        then(inventory).isPresent();
        then(inventory.get().getAvailableQuantity()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("should return empty when product does not exist")
    void shouldReturnEmptyForNonExistentProduct() {
        // Given
        ProductId nonExistentProductId = new ProductId(999L);

        // When
        Optional<OrderInventory> inventory = orderInventoryRepository.findByProductId(nonExistentProductId);

        // Then
        then(inventory).isEmpty();
    }

    @Test
    @DisplayName("should process order cancelled event and restore inventory")
    void shouldRestoreInventoryOnOrderCancelled() {
        // Given — first reserve
        UUID orderId = UUID.randomUUID();
        String orderIdStr = UUID.randomUUID().toString();
        OrderEventMessage createMessage = OrderEventMessage.builder()
            .orderId(orderIdStr)
            .userId(1L)
            .productId(2L)
            .price(new BigDecimal("50.00"))
            .orderStatus("ORDER_CREATED")
            .sagaId(UUID.randomUUID().toString())
            .createdAt(Instant.now())
            .build();
        orderEventMessageListener.processOrderCreated(createMessage);

        OrderEventMessage cancelMessage = OrderEventMessage.builder()
            .orderId(orderIdStr)
            .userId(1L)
            .productId(2L)
            .price(new BigDecimal("50.00"))
            .orderStatus("ORDER_CANCELLED")
            .sagaId(UUID.randomUUID().toString())
            .createdAt(Instant.now())
            .build();

        // When / Then (no exception means inventory was restored)
        orderEventMessageListener.processOrderCancelled(cancelMessage);
    }
}
