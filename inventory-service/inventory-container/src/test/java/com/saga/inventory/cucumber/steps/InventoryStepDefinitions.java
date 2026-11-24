package com.saga.inventory.cucumber.steps;

import com.saga.common.application.events.OrderEventMessage;
import com.saga.common.domain.valueobject.ProductId;
import com.saga.inventory.domain.entity.OrderInventory;
import com.saga.inventory.service.ports.input.message.listener.OrderEventMessageListener;
import com.saga.inventory.service.ports.output.repository.OrderInventoryRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;

public class InventoryStepDefinitions {

    @Autowired
    private OrderEventMessageListener orderEventMessageListener;

    @Autowired
    private OrderInventoryRepository orderInventoryRepository;

    private Long userId;
    private Long productId;
    private String orderId;
    private Exception thrownException;

    @Given("a customer with userId {long} wants to reserve product {long}")
    public void aCustomerWantsToReserveProduct(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
        this.orderId = UUID.randomUUID().toString();
    }

    @When("the inventory reservation is processed")
    public void theInventoryReservationIsProcessed() {
        try {
            OrderEventMessage message = OrderEventMessage.builder()
                .orderId(orderId)
                .userId(userId)
                .productId(productId)
                .price(new BigDecimal("100.00"))
                .orderStatus("ORDER_CREATED")
                .sagaId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .build();
            orderEventMessageListener.processOrderCreated(message);
        } catch (Exception ex) {
            thrownException = ex;
        }
    }

    @Then("the inventory reservation should be processed without errors")
    public void theInventoryReservationShouldBeProcessedWithoutErrors() {
        then(thrownException).isNull();
    }

    @Then("the inventory reservation should fail with error containing {string}")
    public void theInventoryReservationShouldFailWithError(String errorMessage) {
        then(thrownException).isNotNull();
        then(thrownException.getMessage()).contains(errorMessage);
    }

    @Then("the inventory for productId {long} should exist")
    public void theInventoryForProductShouldExist(Long productId) {
        Optional<OrderInventory> inventory = orderInventoryRepository.findByProductId(new ProductId(productId));
        then(inventory).isPresent();
    }

    @Then("the inventory for productId {long} should have available stock")
    public void theInventoryForProductShouldHaveAvailableStock(Long productId) {
        Optional<OrderInventory> inventory = orderInventoryRepository.findByProductId(new ProductId(productId));
        then(inventory).isPresent();
        then(inventory.get().getAvailableQuantity()).isGreaterThan(0);
    }
}
