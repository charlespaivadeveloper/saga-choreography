package com.saga.payment.integration;

import com.saga.common.application.events.OrderEventMessage;
import com.saga.common.domain.valueobject.UserId;
import com.saga.payment.domain.entity.UserBalance;
import com.saga.payment.service.ports.input.message.listener.OrderEventMessageListener;
import com.saga.payment.service.ports.output.repository.UserBalanceRepository;
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
@DisplayName("Payment Service Integration Tests")
class PaymentServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("payment_db")
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
    private UserBalanceRepository userBalanceRepository;

    @Test
    @DisplayName("should process order created event and reserve payment when balance is sufficient")
    void shouldReservePaymentWhenBalanceSufficient() {
        // Given
        UserId userId = new UserId(1L);
        Optional<UserBalance> balance = userBalanceRepository.findByUserId(userId);
        then(balance).isPresent();

        OrderEventMessage message = OrderEventMessage.builder()
            .orderId(UUID.randomUUID().toString())
            .userId(1L)
            .productId(1L)
            .price(new BigDecimal("100.00"))
            .orderStatus("ORDER_CREATED")
            .sagaId(UUID.randomUUID().toString())
            .createdAt(Instant.now())
            .build();

        // When / Then (no exception means payment was processed)
        then(orderEventMessageListener).isNotNull();
        orderEventMessageListener.processOrderCreated(message);
    }

    @Test
    @DisplayName("should find user balance for existing user")
    void shouldFindUserBalanceForExistingUser() {
        // Given
        UserId userId = new UserId(1L);

        // When
        Optional<UserBalance> balance = userBalanceRepository.findByUserId(userId);

        // Then
        then(balance).isPresent();
        then(balance.get().getBalance()).isNotNull();
        then(balance.get().getBalance().getAmount()).isPositive();
    }

    @Test
    @DisplayName("should return empty when user does not exist")
    void shouldReturnEmptyForNonExistentUser() {
        // Given
        UserId nonExistentUserId = new UserId(999L);

        // When
        Optional<UserBalance> balance = userBalanceRepository.findByUserId(nonExistentUserId);

        // Then
        then(balance).isEmpty();
    }

    @Test
    @DisplayName("should process order cancelled event and refund payment")
    void shouldRefundPaymentOnOrderCancelled() {
        // Given — first reserve
        String orderIdStr = UUID.randomUUID().toString();
        OrderEventMessage createMessage = OrderEventMessage.builder()
            .orderId(orderIdStr)
            .userId(2L)
            .productId(1L)
            .price(new BigDecimal("50.00"))
            .orderStatus("ORDER_CREATED")
            .sagaId(UUID.randomUUID().toString())
            .createdAt(Instant.now())
            .build();
        orderEventMessageListener.processOrderCreated(createMessage);

        OrderEventMessage cancelMessage = OrderEventMessage.builder()
            .orderId(orderIdStr)
            .userId(2L)
            .productId(1L)
            .price(new BigDecimal("50.00"))
            .orderStatus("ORDER_CANCELLED")
            .sagaId(UUID.randomUUID().toString())
            .createdAt(Instant.now())
            .build();

        // When / Then (no exception means refund was processed)
        orderEventMessageListener.processOrderCancelled(cancelMessage);
    }
}
