package com.saga.order.integration;

import com.saga.common.domain.valueobject.OrderStatus;
import com.saga.order.service.dto.create.CreateOrderCommand;
import com.saga.order.service.dto.create.CreateOrderResponse;
import com.saga.order.service.ports.input.service.OrderApplicationService;
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

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Order Service Integration Tests")
class OrderServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("order_db")
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
    private OrderApplicationService orderApplicationService;

    @Test
    @DisplayName("should create order successfully with valid command")
    void shouldCreateOrderSuccessfullyWithValidCommand() {
        // Given
        CreateOrderCommand command = CreateOrderCommand.builder()
            .userId(1L)
            .productId(1L)
            .build();

        // When
        CreateOrderResponse response = orderApplicationService.createOrder(command);

        // Then
        then(response).isNotNull();
        then(response.getOrderId()).isNotNull();
        then(response.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CREATED);
        then(response.getMessage()).isEqualTo("Order created successfully");
    }

    @Test
    @DisplayName("should return all orders")
    void shouldReturnAllOrders() {
        // Given
        CreateOrderCommand command = CreateOrderCommand.builder()
            .userId(2L)
            .productId(2L)
            .build();
        orderApplicationService.createOrder(command);

        // When
        var response = orderApplicationService.getAllOrders();

        // Then
        then(response).isNotNull();
        then(response.getOrders()).isNotEmpty();
    }
}
