package com.saga.order.cucumber.steps;

import com.saga.common.domain.valueobject.OrderStatus;
import com.saga.order.service.dto.create.CreateOrderCommand;
import com.saga.order.service.dto.create.CreateOrderResponse;
import com.saga.order.service.ports.input.service.OrderApplicationService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.BDDAssertions.then;

public class OrderStepDefinitions {

    @Autowired
    private OrderApplicationService orderApplicationService;

    private CreateOrderCommand createOrderCommand;
    private CreateOrderResponse createOrderResponse;
    private Exception thrownException;

    @Given("a customer with userId {long} wants to buy product {long}")
    public void aCustomerWantsToBuyProduct(Long userId, Long productId) {
        createOrderCommand = CreateOrderCommand.builder()
            .userId(userId)
            .productId(productId)
            .build();
    }

    @When("the customer creates an order")
    public void theCustomerCreatesAnOrder() {
        try {
            createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
        } catch (Exception ex) {
            thrownException = ex;
        }
    }

    @Then("the order should be created successfully")
    public void theOrderShouldBeCreatedSuccessfully() {
        then(createOrderResponse).isNotNull();
        then(createOrderResponse.getOrderId()).isNotNull();
        then(createOrderResponse.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CREATED);
    }

    @Then("the order response message should be {string}")
    public void theOrderResponseMessageShouldBe(String expectedMessage) {
        then(createOrderResponse.getMessage()).isEqualTo(expectedMessage);
    }

    @Then("the order creation should fail with error containing {string}")
    public void theOrderCreationShouldFailWithError(String errorMessage) {
        then(thrownException).isNotNull();
        then(thrownException.getMessage()).contains(errorMessage);
    }
}
