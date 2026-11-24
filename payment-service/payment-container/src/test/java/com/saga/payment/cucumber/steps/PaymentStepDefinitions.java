package com.saga.payment.cucumber.steps;

import com.saga.common.application.events.OrderEventMessage;
import com.saga.common.domain.valueobject.UserId;
import com.saga.payment.domain.entity.UserBalance;
import com.saga.payment.service.ports.input.message.listener.OrderEventMessageListener;
import com.saga.payment.service.ports.output.repository.UserBalanceRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;

public class PaymentStepDefinitions {

    @Autowired
    private OrderEventMessageListener orderEventMessageListener;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    private Long userId;
    private Long productId;
    private BigDecimal price;
    private String orderId;
    private Exception thrownException;
    private Optional<UserBalance> userBalance;

    @Given("a user with userId {long} has sufficient balance for price {bigdecimal}")
    public void aUserWithSufficientBalance(Long userId, BigDecimal price) {
        this.userId = userId;
        this.price = price;
        this.orderId = UUID.randomUUID().toString();
        this.userBalance = userBalanceRepository.findByUserId(new UserId(userId));
        then(this.userBalance).isPresent();
        then(this.userBalance.get().getBalance().getAmount()).isGreaterThanOrEqualTo(price);
    }

    @Given("a user with userId {long} wants to pay for product {long}")
    public void aUserWantsToPayForProduct(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
        this.orderId = UUID.randomUUID().toString();
    }

    @When("the payment is processed")
    public void thePaymentIsProcessed() {
        try {
            OrderEventMessage message = OrderEventMessage.builder()
                .orderId(orderId)
                .userId(userId)
                .productId(productId != null ? productId : 1L)
                .price(price != null ? price : new BigDecimal("100.00"))
                .orderStatus("ORDER_CREATED")
                .sagaId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .build();
            orderEventMessageListener.processOrderCreated(message);
        } catch (Exception ex) {
            thrownException = ex;
        }
    }

    @Then("the payment should be processed without errors")
    public void thePaymentShouldBeProcessedWithoutErrors() {
        then(thrownException).isNull();
    }

    @Then("the payment processing should fail with error containing {string}")
    public void thePaymentProcessingShouldFailWithError(String errorMessage) {
        then(thrownException).isNotNull();
        then(thrownException.getMessage()).contains(errorMessage);
    }

    @Then("the user balance for userId {long} should exist")
    public void theUserBalanceShouldExist(Long userId) {
        Optional<UserBalance> balance = userBalanceRepository.findByUserId(new UserId(userId));
        then(balance).isPresent();
    }
}
